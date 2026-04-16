package monokai.whisperapilab.utils

import org.springframework.web.multipart.MultipartFile
import kotlin.concurrent.thread

fun extractAudioInMemory(file: MultipartFile): ByteArray {
    val processBuilder = ProcessBuilder(
        "ffmpeg", "-i", "pipe:0", // Lê da entrada padrão
        "-vn", "-acodec", "pcm_s16le", "-ar", "16000", "-ac", "1",
        "-f", "wav", "pipe:1" // Força formato WAV e envia para saída padrão
    )

    val process = processBuilder.start()

    // IMPORTANTE: Use threads para não travar o buffer do processo
    // Escreve o vídeo no STDIN do FFmpeg
    thread {
        process.outputStream.use { it.write(file.bytes) }
    }

    // Lê o áudio do STDOUT do FFmpeg
    val audioData = process.inputStream.readBytes()

    process.waitFor()
    return audioData
}
