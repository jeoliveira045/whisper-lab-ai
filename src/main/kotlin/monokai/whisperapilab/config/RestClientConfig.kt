package monokai.whisperapilab.config;

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class RestClientConfig {

    @Value("\${transcription.api.url}")
    private val apiUrl: String? = null

    @Value("\${transcription.api.key}")
    private val apiKey: String? = null

    @Bean
    fun restClient(): RestClient {
        return RestClient.builder()
            .baseUrl(apiUrl!!)
            .defaultHeader("Authorization", "Bearer " + apiKey)
            .build()
    }
}
