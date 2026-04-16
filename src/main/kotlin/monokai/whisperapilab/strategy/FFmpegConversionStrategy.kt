package monokai.whisperapilab.strategy

import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import kotlin.concurrent.thread

@Component
class FFmpegConversionStrategy : AudioConversionStrategy {
    
    override fun convert(file: MultipartFile): ByteArray {
        val processBuilder = ProcessBuilder(
            "ffmpeg", "-i", "pipe:0",
            "-vn", "-acodec", "pcm_s16le", "-ar", "16000", "-ac", "1",
            "-f", "wav", "pipe:1"
        )

        val process = processBuilder.start()

        thread {
            process.outputStream.use { it.write(file.bytes) }
        }

        val audioData = process.inputStream.readBytes()
        process.waitFor()
        
        return audioData
    }
    
    override fun supports(filename: String): Boolean {
        val supportedFormats = listOf(".mp4", ".mp3", ".avi", ".mov", ".mkv", ".webm", ".m4a", ".flac", ".ogg")
        return supportedFormats.any { filename.endsWith(it, ignoreCase = true) }
    }
}
