package monokai.whisperapilab.strategy

import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component
class AudioConversionContext(
    private val strategies: List<AudioConversionStrategy>
) {
    
    fun convertAudio(file: MultipartFile): ByteArray {
        val filename = file.originalFilename ?: throw IllegalArgumentException("Nome do arquivo não pode ser nulo")
        
        val strategy = strategies.firstOrNull { it.supports(filename) }
            ?: throw IllegalArgumentException("Formato de arquivo não suportado: $filename")
        
        return strategy.convert(file)
    }
}
