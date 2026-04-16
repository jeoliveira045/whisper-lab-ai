package monokai.whisperapilab.adapter

import org.springframework.web.multipart.MultipartFile

interface MediaProcessor {
    fun getDuration(file: MultipartFile): Double
    fun splitFile(file: MultipartFile, maxDurationSeconds: Int): List<MultipartFile>
    fun convertToAudio(file: MultipartFile): ByteArray
}
