package monokai.whisperapilab.service

import com.google.api.gax.rpc.ApiStreamObserver
import com.google.api.gax.rpc.BidiStreamingCallable
import com.google.protobuf.ByteString
import com.google.cloud.speech.v1.*
import monokai.whisperapilab.utils.extractAudioInMemory
import monokai.whisperapilab.utils.splitAudioIntoChunks
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.*



@Service
class TranscriptionsService {


    fun transcription(arquivo: MultipartFile): String = runBlocking {
        val chunks = splitAudioIntoChunks(arquivo, 120)
        
        val results = chunks.mapIndexed { index, chunk ->
            async(Dispatchers.IO) {
                println("Processando parte ${index + 1} de ${chunks.size}")
                index to transcribeChunk(chunk)
            }
        }.awaitAll()
        
        results.sortedBy { it.first }
            .joinToString(" ") { it.second }
            .trim()
    }
    
    private fun transcribeChunk(arquivo: MultipartFile): String {
        var audioBytes : ByteArray
        val latch = CountDownLatch(1)
        val stringBuilder: StringBuilder = StringBuilder()

        SpeechClient.create().use { speechClient ->

            if(arquivo.originalFilename!!.contains(".wav")) audioBytes = arquivo.bytes
            else audioBytes = extractAudioInMemory(arquivo)

            // 1. O Observer que escuta as respostas do Google
            val responseObserver = object : ApiStreamObserver<StreamingRecognizeResponse> {
                override fun onNext(response: StreamingRecognizeResponse) {
                    if (response.resultsCount > 0) {
                        val result = response.resultsList[0]
                        if (result.alternativesCount > 0) {
                            val alternative = result.alternativesList[0]
                            if(result.isFinal) stringBuilder.append(alternative.transcript).append(" ")
                        }
                    }
                }

                override fun onError(t: Throwable) {
                    println("Erro na transcrição: ${t.message}")
                    latch.countDown()
                }

                override fun onCompleted() {
                    latch.countDown()
                }
            }

            // 2. Abre o "canal" de escrita (Stream)k
            val callable: BidiStreamingCallable<StreamingRecognizeRequest, StreamingRecognizeResponse> =
                speechClient.streamingRecognizeCallable()

            val requestObserver = callable.bidiStreamingCall(responseObserver)

            // 3. A primeira requisição DEVE conter a configuração
            val config = RecognitionConfig.newBuilder()
                .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                .setSampleRateHertz(16000)
                .setLanguageCode("pt-BR")
                .build()

            val streamingConfig = StreamingRecognitionConfig.newBuilder()
                .setConfig(config)
                .setInterimResults(false)
                .build()

            requestObserver.onNext(
                StreamingRecognizeRequest.newBuilder().setStreamingConfig(streamingConfig).build()
            )

            // 4. "Empurra" o áudio em pedaços (chunks)
            val chunkSize = 8000
            var offset = 0
            while (offset < audioBytes.size) {
                val end = minOf(offset + chunkSize, audioBytes.size)
                val request = StreamingRecognizeRequest.newBuilder()
                    .setAudioContent(ByteString.copyFrom(audioBytes, offset, end - offset))
                    .build()
                requestObserver.onNext(request)
                offset = end
                Thread.sleep(50)
            }

            requestObserver.onCompleted()
            
            if (!latch.await(5, TimeUnit.MINUTES)) {
                throw RuntimeException("Timeout ao processar chunk de áudio")
            }
        }

        return stringBuilder.toString().trim()
    }
}