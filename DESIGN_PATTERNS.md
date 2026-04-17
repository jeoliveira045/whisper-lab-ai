# Design Patterns Implementados

Este documento descreve os design patterns implementados no projeto Whisper API Lab.

## 1. Strategy Pattern

**Localização:** `strategy/`

**Propósito:** Permitir diferentes estratégias de conversão de áudio baseadas no formato do arquivo.

**Componentes:**
- `AudioConversionStrategy` - Interface que define o contrato
- `WavConversionStrategy` - Estratégia para arquivos WAV (sem conversão)
- `FFmpegConversionStrategy` - Estratégia para outros formatos (usa FFmpeg)
- `AudioConversionContext` - Contexto que seleciona a estratégia apropriada

**Benefícios:**
- Fácil adicionar novos formatos de áudio
- Código mais testável e modular
- Separação de responsabilidades

**Exemplo de uso:**
```kotlin
@Service
class TranscriptionsService(
    private val audioConversionContext: AudioConversionContext
) {
    fun process(file: MultipartFile) {
        val audioBytes = audioConversionContext.convertAudio(file)
        // ...
    }
}
```

## 2. Factory Pattern

**Localização:** `factory/`

**Propósito:** Centralizar a criação de configurações complexas do Google Speech API.

**Componentes:**
- `SpeechConfigFactory` - Factory que cria configurações do Speech API

**Benefícios:**
- Configurações centralizadas e reutilizáveis
- Fácil manutenção e alteração de parâmetros
- Reduz duplicação de código

**Exemplo de uso:**
```kotlin
@Service
class TranscriptionsService(
    private val speechConfigFactory: SpeechConfigFactory
) {
    fun transcribe() {
        val config = speechConfigFactory.createDefaultStreamingConfig()
        // ou
        val customConfig = speechConfigFactory.createRecognitionConfig(
            languageCode = "en-US",
            sampleRateHertz = 48000
        )
    }
}
```

## 3. Adapter Pattern

**Localização:** `adapter/`

**Propósito:** Fornecer uma interface unificada para operações com FFmpeg.

**Componentes:**
- `MediaProcessor` - Interface que define operações de processamento de mídia
- `FFmpegAdapter` - Implementação que adapta chamadas FFmpeg

**Benefícios:**
- Abstração das chamadas ao FFmpeg
- Fácil substituir por outra biblioteca de processamento
- Código mais testável (pode criar mock do adapter)

**Exemplo de uso:**
```kotlin
@Service
class TranscriptionsService(
    private val mediaProcessor: MediaProcessor
) {
    fun process(file: MultipartFile) {
        val duration = mediaProcessor.getDuration(file)
        val chunks = mediaProcessor.splitFile(file, 300)
        // ...
    }
}
```

## Arquitetura Resultante

```
TranscriptionsService
    ├── MediaProcessor (Adapter)
    │   └── FFmpegAdapter
    ├── SpeechConfigFactory (Factory)
    └── AudioConversionContext (Strategy)
        ├── WavConversionStrategy
        └── FFmpegConversionStrategy
```

## Vantagens da Implementação

1. **Manutenibilidade**: Código mais organizado e fácil de manter
2. **Testabilidade**: Cada componente pode ser testado isoladamente
3. **Extensibilidade**: Fácil adicionar novos formatos, configurações ou processadores
4. **Separação de Responsabilidades**: Cada classe tem uma única responsabilidade
5. **Inversão de Dependência**: Service depende de abstrações, não de implementações concretas

## Como Adicionar Novos Recursos

### Adicionar novo formato de áudio:
```kotlin
@Component
class Mp3ConversionStrategy : AudioConversionStrategy {
    override fun convert(file: MultipartFile): ByteArray {
        // Implementação específica para MP3
    }
    
    override fun supports(filename: String): Boolean {
        return filename.endsWith(".mp3", ignoreCase = true)
    }
}
```

### Adicionar nova configuração:
```kotlin
// Em SpeechConfigFactory
fun createHighQualityConfig(): RecognitionConfig {
    return createRecognitionConfig(
        sampleRateHertz = 48000,
        encoding = RecognitionConfig.AudioEncoding.FLAC
    )
}
```

### Substituir FFmpeg por outra biblioteca:
```kotlin
@Component
class CustomMediaProcessor : MediaProcessor {
    // Implementação usando outra biblioteca
}
```
