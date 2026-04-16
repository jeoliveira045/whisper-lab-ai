package monokai.whisperapilab.service


import org.springframework.ai.chat.client.ChatClient
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class AnalyzerService(chatClientBuilder: ChatClient.Builder) {

    private var chatClient: ChatClient = chatClientBuilder.build()

    fun gerarResposta(prompt: String): Flux<String> {
        val response = this.chatClient.prompt()
            .user(prompt)
            .system("Crie um resumo do audio em questão")
            .stream().content()
        return response
    }
}