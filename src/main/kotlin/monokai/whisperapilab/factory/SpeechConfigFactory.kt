package monokai.whisperapilab.factory

import com.google.cloud.speech.v1.RecognitionConfig
import com.google.cloud.speech.v1.StreamingRecognitionConfig
import org.springframework.stereotype.Component

@Component
class SpeechConfigFactory {
    
    fun createRecognitionConfig(
        languageCode: String = "pt-BR",
        sampleRateHertz: Int = 16000,
        encoding: RecognitionConfig.AudioEncoding = RecognitionConfig.AudioEncoding.LINEAR16
    ): RecognitionConfig {
        return RecognitionConfig.newBuilder()
            .setEncoding(encoding)
            .setSampleRateHertz(sampleRateHertz)
            .setLanguageCode(languageCode)
            .build()
    }
    
    fun createStreamingConfig(
        recognitionConfig: RecognitionConfig,
        interimResults: Boolean = false
    ): StreamingRecognitionConfig {
        return StreamingRecognitionConfig.newBuilder()
            .setConfig(recognitionConfig)
            .setInterimResults(interimResults)
            .build()
    }
    
    fun createDefaultStreamingConfig(): StreamingRecognitionConfig {
        val config = createRecognitionConfig()
        return createStreamingConfig(config)
    }
}
