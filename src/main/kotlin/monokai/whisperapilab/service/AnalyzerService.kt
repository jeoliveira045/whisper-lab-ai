package monokai.whisperapilab.service

import org.springframework.ai.chat.client.ChatClient
import org.springframework.stereotype.Service

@Service
class AnalyzerService(private val chatClient: ChatClient.Builder) {

    private val client = chatClient.build()

    fun analyze(texto: String): String? {
        return client.prompt()
            .system("Você é um assistente que analisa transcrições de áudio.")
            .user(texto)
            .call()
            .content()
    }
}