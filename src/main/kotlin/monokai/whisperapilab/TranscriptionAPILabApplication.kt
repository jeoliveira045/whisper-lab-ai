package monokai.whisperapilab

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TranscriptionAPILabApplication

fun main(args: Array<String>) {
    runApplication<TranscriptionAPILabApplication>(*args)
}
