package monokai.whisperapilab.config

import com.openai.client.okhttp.OpenAIOkHttpClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenAIConfig {

    @Value("\${transcription.api.key}")
    private val apiKey: String? = null;

    @Bean
    fun openAiConfiguration() = OpenAIOkHttpClient.builder().apiKey(this.apiKey!!).build()
}