package monokai.whisperapilab.config

import jakarta.annotation.PostConstruct
import org.springframework.ai.model.google.genai.autoconfigure.chat.GoogleGenAiConnectionProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.Environment
import java.io.File


@Configuration
class GeminiConfiguration(
    private val properties: GoogleGenAiConnectionProperties,
) {

    @PostConstruct
    fun loadEnvironmentVariables() {
        val envFile = File(".env")

        if (!envFile.exists()) {
            println("Arquivo .env não encontrado")
            return
        }

        envFile.readLines()
            .filter { it.isNotBlank() && !it.startsWith("#") }
            .forEach { line ->
                val parts = line.split("=", limit = 2)
                if (parts.size == 2) {
                    val key = parts[0].trim()
                    val value = parts[1].trim()
                    System.setProperty(key, value)
                    println("Variável carregada: $key")
                }
            }

        println("Variáveis de ambiente carregadas do .env")

        properties.apiKey = System.getProperty("GEMINI_API_KEY")
    }

}