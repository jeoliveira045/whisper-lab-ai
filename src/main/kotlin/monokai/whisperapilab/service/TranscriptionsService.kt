package monokai.whisperapilab.service

import com.google.cloud.speech.v1.SpeechClient
import monokai.whisperapilab.adapter.MediaProcessor
import monokai.whisperapilab.factory.SpeechConfigFactory
import monokai.whisperapilab.strategy.AudioConversionContext
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.*



@Service
class TranscriptionsService(
    private val mediaProcessor: MediaProcessor,
    private val speechConfigFactory: SpeechConfigFactory,
    private val audioConversionContext: AudioConversionContext
) {
    companion object {
        private const val CHUNK_DURATION_SECONDS = 120
        private const val TIMEOUT_MINUTES = 5L
        private const val AUDIO_CHUNK_SIZE = 8000
        private const val STREAM_DELAY_MS = 50L
    }


    fun transcription(arquivo: MultipartFile): String = runBlocking {
        val chunks = mediaProcessor.splitFile(arquivo, CHUNK_DURATION_SECONDS)
        
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
        val latch = CountDownLatch(1)
        val resultBuilder = StringBuilder()

        SpeechClient.create().use { speechClient ->
            val audioBytes = audioConversionContext.convertAudio(arquivo)
            val streamingConfig = speechConfigFactory.createDefaultStreamingConfig()
            
            val responseObserver = TranscriptionResponseObserver(latch, resultBuilder)
            val callable = speechClient.streamingRecognizeCallable()
            val streamHandler = AudioStreamHandler(callable, responseObserver)
            
            streamHandler.sendConfig(streamingConfig)
            streamHandler.streamAudio(audioBytes, AUDIO_CHUNK_SIZE, STREAM_DELAY_MS)
            streamHandler.complete()
            
            if (!latch.await(TIMEOUT_MINUTES, TimeUnit.MINUTES)) {
                throw RuntimeException("Timeout ao processar chunk de áudio após $TIMEOUT_MINUTES minutos")
            }
        }

        return resultBuilder.toString().trim()
    }
}