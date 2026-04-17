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
        /**
         * Quebrar o arquivo em partes menores para serem processados pela API
         */
        val chunks = mediaProcessor.splitFile(arquivo, CHUNK_DURATION_SECONDS)

        /**
         * Aqui é realizado o processamento das partes pela API. Primeiro,
         * um mapa indexado das partes é criado, para então serem processados de forma
         * assincrona pelo sistema pelo metodo async. De forma paralela, todos os asyncs
         * iniciados retornarão um Pair com o index da parte e a transcrição. O metodo awaitAll()
         * obriga o programa a esperar todos os processamentos serem finalizados para então da
         * sequência as outras instruções
         */

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
        /**
         * Cada uma das partes é processada aqui
         */
        val latch = CountDownLatch(1)
        val resultBuilder = StringBuilder()

        SpeechClient.create().use { speechClient ->
            //1. O audio é convertido para ByteArray para poder ser executado em tempo de execução
            val audioBytes = audioConversionContext.convertAudio(arquivo)
            //2. Configura os parametros do transcritor. Linguagem, rate de frequencia e encoding são configurados aqui
            val streamingConfig = speechConfigFactory.createDefaultStreamingConfig()
            //3. Aqui é configurado a transcrição sera armazenada. Isso é necessario pois o sistema realiza a transcrição aos poucos(streaming)
            // e por isso ela é armazenada aos poucos(StringBuilder). Sem isso a transcrição pode ser feita de forma repetitiva, o que geraria strings muito grandes para serem processadas
            val responseObserver = TranscriptionResponseObserver(latch, resultBuilder)
            //4. Aqui é o metodo que irá performar a transcrição de forma bidirecional: É enviado o audio enquanto se recebe a transcrição do mesmo
            val callable = speechClient.streamingRecognizeCallable()
            //5. O utilitario do sistema que aplica as configurações e executa o processo de transcrição.
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