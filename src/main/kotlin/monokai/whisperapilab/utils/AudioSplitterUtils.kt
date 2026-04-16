package monokai.whisperapilab.utils

import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files

fun splitAudioIntoChunks(file: MultipartFile, maxDurationSeconds: Int = 300): List<MultipartFile> {
//    Aqui é declarado a lista de multipartfiles que vai vão ser transcritos paralelamente
    val chunks = mutableListOf<MultipartFile>()
    val originalFilename = file.originalFilename ?: "audio"
//    Um arquivo temporario é criado para que o process builder possa realizar as devidas alterações
    val tempInputFile = Files.createTempFile("input_", "_${originalFilename}").toFile()
    tempInputFile.writeBytes(file.bytes)
    
    try {
//        Comando para que o computador possa ser realizado com os sistemas
        val processBuilder = ProcessBuilder(
            "ffprobe", "-v", "error", "-show_entries", "format=duration",
            "-of", "default=noprint_wrappers=1:nokey=1", tempInputFile.absolutePath
        )
        
        val probeProcess = processBuilder.start()
        val duration = probeProcess.inputStream.bufferedReader().readText().trim().toDoubleOrNull() ?: 0.0
        probeProcess.waitFor()
        
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
