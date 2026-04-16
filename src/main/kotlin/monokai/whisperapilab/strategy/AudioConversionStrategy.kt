package monokai.whisperapilab.strategy

import org.springframework.web.multipart.MultipartFile

interface AudioConversionStrategy {
    fun convert(file: MultipartFile): ByteArray
    fun supports(filename: String): Boolean
}
