package monokai.whisperapilab.strategy

import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component
class WavConversionStrategy : AudioConversionStrategy {
    
    override fun convert(file: MultipartFile): ByteArray {
        return file.bytes
    }
    
    override fun supports(filename: String): Boolean {
        return filename.endsWith(".wav", ignoreCase = true)
    }
}
