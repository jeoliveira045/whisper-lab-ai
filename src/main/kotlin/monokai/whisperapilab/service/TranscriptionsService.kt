package monokai.whisperapilab.service

import monokai.whisperapilab.model.Response
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import org.springframework.web.multipart.MultipartFile
import tools.jackson.module.kotlin.jacksonObjectMapper


@Service
class TranscriptionsService(private val restClient: RestClient) {

    fun transcription(arquivo: MultipartFile): String? {
        val resource: Resource = arquivo.resource

        val responseAsString = restClient.post()
            .uri("/audio/transcriptions")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(object : LinkedMultiValueMap<String, Any>() {
                init {
                    add("file", resource)
                    add("model", "whisper-1")
                }
            })
            .retrieve()
            .body<String>()

        val om = jacksonObjectMapper()

        val transcription = om.readValue(responseAsString, Response::class.java).text

        return transcription
    }
}