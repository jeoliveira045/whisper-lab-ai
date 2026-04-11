package monokai.whisperapilab.service


import com.openai.client.OpenAIClient
import com.openai.models.ChatCompletionCreateParams
import com.openai.models.ChatModel
import org.springframework.stereotype.Service

@Service
class AnalyzerService(private val chatClient: OpenAIClient) {

    fun analyze(texto: String): String? {
        val params = ChatCompletionCreateParams.builder().addUserMessage("faça um resume do seguinte texto: $texto").model(ChatModel.GPT_4).build()
        val chatCompletion = chatClient.chat().completions().create(params)
        val response = chatCompletion.choices().first().message().content().orElse("empty")
        return response
    }
}