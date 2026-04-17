package monokai.whisperapilab.adapter

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.springframework.mock.web.MockMultipartFile
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files

@Component
class FFmpegAdapter : MediaProcessor {
    
    override fun getDuration(file: MultipartFile): Double {
        val tempFile = Files.createTempFile("probe_", "_${file.originalFilename}").toFile()
        
        try {
            tempFile.writeBytes(file.bytes)
            
            val processBuilder = ProcessBuilder(
                "ffprobe", "-v", "error", "-show_entries", "format=duration",
                "-of", "default=noprint_wrappers=1:nokey=1", tempFile.absolutePath
            )
            
            val process = processBuilder.start()
            val duration = process.inputStream.bufferedReader().readText().trim().toDoubleOrNull() ?: 0.0
            process.waitFor()
            
            return duration
        } finally {
            tempFile.delete()
        }
    }
    
    override fun splitFile(file: MultipartFile, maxDurationSeconds: Int): List<MultipartFile> {
        val chunks = mutableListOf<MultipartFile>()
        val originalFilename = file.originalFilename ?: "audio"
        
        val tempInputFile = Files.createTempFile("input_", "_${originalFilename}").toFile()
        tempInputFile.writeBytes(file.bytes)
        
        try {
            val duration = getDuration(file)
            
            if (duration <= maxDurationSeconds) {
                return listOf(file)
            }
            
            val numChunks = (duration / maxDurationSeconds).toInt() + 1
            
            for (i in 0 until numChunks) {
                val startTime = i * maxDurationSeconds
                val tempOutputFile = Files.createTempFile("output_part${i}_", ".wav").toFile()
                
                val ffmpegProcess = ProcessBuilder(
                    "ffmpeg", "-i", tempInputFile.absolutePath,
                    "-ss", startTime.toString(),
                    "-t", maxDurationSeconds.toString(),
                    "-vn", "-acodec", "pcm_s16le", "-ar", "16000", "-ac", "1",
                    "-y", tempOutputFile.absolutePath
                ).start()
                
                ffmpegProcess.waitFor()
                
                if (tempOutputFile.exists() && tempOutputFile.length() > 0) {
                    chunks.add(
                        MockMultipartFile(
                            "file",
                            "${originalFilename.substringBeforeLast(".")}_part${i + 1}.wav",
                            "audio/wav",
                            tempOutputFile.readBytes()
                        )
                    )
                }
                
                tempOutputFile.delete()
            }
        } finally {
            tempInputFile.delete()
        }
        
        return chunks
    }
    
    override fun convertToAudio(file: MultipartFile): ByteArray = runBlocking {
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
}
