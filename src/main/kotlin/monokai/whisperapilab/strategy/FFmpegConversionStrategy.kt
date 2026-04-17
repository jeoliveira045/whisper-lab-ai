package monokai.whisperapilab.strategy

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component
class FFmpegConversionStrategy : AudioConversionStrategy {
    
    override fun convert(file: MultipartFile): ByteArray = runBlocking {
        val processBuilder = ProcessBuilder(
            "ffmpeg", "-i", "pipe:0",
            "-vn", "-acodec", "pcm_s16le", "-ar", "16000", "-ac", "1",
            "-f", "wav", "pipe:1"
        )

        val process = processBuilder.start()

        val writeJob = async(Dispatchers.IO) {
            process.outputStream.use { it.write(file.bytes) }
        }

        val readJob = async(Dispatchers.IO) {
            process.inputStream.readBytes()
        }

        val audioData = readJob.await()
        writeJob.await()
        process.waitFor()
        
        audioData
    }
    
    override fun supports(filename: String): Boolean {
        val supportedFormats = listOf(".mp4", ".mp3", ".avi", ".mov", ".mkv", ".webm", ".m4a", ".flac", ".ogg")
        return supportedFormats.any { filename.endsWith(it, ignoreCase = true) }
    }
}
