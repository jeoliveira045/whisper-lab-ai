package monokai.whisperapilab.controller

import monokai.whisperapilab.service.AnalyzerService
import monokai.whisperapilab.service.TranscriptionsService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile


@RestController
class WhisperAPIController(
    private val transcriptionsService: TranscriptionsService,
    private val analyzerService: AnalyzerService
) {

    @PostMapping("/whisper")
    fun helloWhisper(@RequestParam("file") file: MultipartFile) =
            analyzerService.analyze(transcriptionsService.transcription(file)!!)

}