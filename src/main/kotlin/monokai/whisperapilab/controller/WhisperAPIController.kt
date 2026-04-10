package monokai.whisperapilab.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController


@RestController
class WhisperAPIController {

    @GetMapping("/whisper")
    public fun helloWhisper() = "Hello World!"

}