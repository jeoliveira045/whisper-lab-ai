# Whisper API Lab

API REST profissional para transcrição de áudio e vídeo usando Google Cloud Speech-to-Text com processamento paralelo, design patterns e integração com Google Gemini AI.

## 📋 Descrição

Este projeto oferece uma solução robusta e escalável para transcrição de arquivos de áudio e vídeo longos. Utiliza arquitetura baseada em design patterns (Strategy, Factory, Adapter), divide arquivos automaticamente em chunks de até 2 minutos e processa-os em paralelo usando Kotlin Coroutines. A API também integra o Google Gemini AI para análise e sumarização de transcrições.

## ✨ Funcionalidades

- 🎵 **Transcrição de áudio/vídeo**: Suporte para múltiplos formatos (WAV, MP4, MP3, AVI, MOV, MKV, WEBM, M4A, FLAC, OGG)
- ⚡ **Processamento paralelo**: Usa Kotlin Coroutines para processar múltiplos chunks simultaneamente
- 📦 **Divisão automática**: Arquivos longos são divididos em partes de até 2 minutos
- 🔄 **Conversão inteligente**: Sistema de estratégias para conversão baseada no formato
- 🤖 **Integração com Gemini AI**: Análise e sumarização de transcrições com Google Gemini
- 📤 **Upload de arquivos grandes**: Suporta arquivos de até 500MB
- 🏗️ **Arquitetura limpa**: Implementa design patterns (Strategy, Factory, Adapter, Observer)
- 🔧 **Configuração automática**: Carregamento automático de variáveis de ambiente do arquivo .env

## 🛠️ Tecnologias

- **Kotlin** 2.2.21
- **Spring Boot** 4.0.5
- **Google Cloud Speech-to-Text** 4.78.0
- **Google Gemini AI** (Spring AI 1.1.0)
- **Kotlin Coroutines** 1.10.0
- **FFmpeg** (para processamento de áudio/vídeo)
- **Gradle** 9.4.1

## 🎨 Design Patterns Implementados

### Strategy Pattern
Permite diferentes estratégias de conversão de áudio baseadas no formato do arquivo:
- `WavConversionStrategy` - Para arquivos WAV (sem conversão)
- `FFmpegConversionStrategy` - Para outros formatos (usa FFmpeg)

### Factory Pattern
Centraliza a criação de configurações do Google Speech API:
- `SpeechConfigFactory` - Cria configurações padronizadas e customizadas

### Adapter Pattern
Fornece interface unificada para operações com FFmpeg:
- `MediaProcessor` - Interface abstrata
- `FFmpegAdapter` - Implementação concreta

### Observer Pattern
Processa respostas assíncronas da API:
- `TranscriptionResponseObserver` - Observa e processa resultados

## 📋 Pré-requisitos

### Software necessário

1. **Java 17** ou superior
2. **FFmpeg** instalado e disponível no PATH
   ```bash
   # Ubuntu/Debian
   sudo apt-get install ffmpeg
   
   # macOS
   brew install ffmpeg
   
   # Windows
   # Baixe de https://ffmpeg.org/download.html
   ```

### Credenciais necessárias

1. **Google Cloud Speech-to-Text**
   - Crie um projeto no [Google Cloud Console](https://console.cloud.google.com/)
   - Ative a API Speech-to-Text
   - Crie uma Service Account e baixe o arquivo JSON de credenciais
   - Coloque o arquivo JSON na raiz do projeto

2. **Google Gemini API**
   - Obtenha uma API Key em [Google AI Studio](https://makersuite.google.com/app/apikey)

## 🚀 Instalação e Configuração

### 1. Clone o repositório

```bash
git clone https://github.com/seu-usuario/whisper-api-lab.git
cd whisper-api-lab
```

### 2. Configure as variáveis de ambiente

Crie um arquivo `.env` na raiz do projeto:

```env
# Google Cloud Speech-to-Text (caminho relativo ou absoluto)
GOOGLE_APPLICATION_CREDENTIALS=gen-lang-client-0611056313-9a38dc1837c2.json

# Google Gemini AI
GEMINI_API_KEY=sua-api-key-aqui
```

**Nota:** O arquivo `.env` é carregado automaticamente pela classe `GeminiConfiguration` usando `@PostConstruct`.

### 3. Execute o projeto

```bash
# Linux/macOS
./gradlew bootRun

# Windows
gradlew.bat bootRun
```

A aplicação estará disponível em `http://localhost:8080`

## 📡 Endpoints da API

### POST /transcription

Transcreve um arquivo de áudio ou vídeo e retorna um resumo gerado pelo Gemini AI.

**Request:**
```bash
curl -X POST http://localhost:8080/transcription \
  -F "file=@/caminho/para/seu/arquivo.mp4"
```

**Response (Stream):**
```text
Resumo da transcrição gerado pelo Gemini AI...
```

**Formatos suportados:**
- **Áudio:** WAV, MP3, M4A, FLAC, OGG
- **Vídeo:** MP4, AVI, MOV, MKV, WEBM

**Características:**
- Arquivos são automaticamente divididos em chunks de 2 minutos
- Processamento paralelo para maior velocidade
- Conversão automática para formato WAV 16kHz mono
- Timeout de 5 minutos por chunk
- Resposta em streaming do Gemini AI

## 🏗️ Arquitetura

```
whisper-api-lab/
├── adapter/                          # Adapter Pattern
│   ├── MediaProcessor.kt            # Interface
│   └── FFmpegAdapter.kt             # Implementação FFmpeg
├── strategy/                         # Strategy Pattern
│   ├── AudioConversionStrategy.kt   # Interface
│   ├── WavConversionStrategy.kt     # Estratégia WAV
│   ├── FFmpegConversionStrategy.kt  # Estratégia FFmpeg
│   └── AudioConversionContext.kt    # Contexto
├── factory/                          # Factory Pattern
│   └── SpeechConfigFactory.kt       # Factory de configurações
├── service/                          # Lógica de negócio
│   ├── TranscriptionsService.kt     # Orquestrador principal
│   ├── TranscriptionResponseObserver.kt  # Observer Pattern
│   ├── AudioStreamHandler.kt        # Gerenciador de streaming
│   └── AnalyzerService.kt           # Integração Gemini AI
├── controller/                       # Endpoints REST
│   └── TranscriptionAPIController.kt
├── config/                           # Configurações
│   └── GeminiConfiguration.kt       # Carrega .env
└── model/                            # Modelos de dados
    ├── Response.kt
    └── Usage.kt
```

### Fluxo de processamento

```
1. Upload do arquivo
   ↓
2. MediaProcessor.splitFile() → Divide em chunks de 2 minutos
   ↓
3. Processamento paralelo (Coroutines)
   ├─→ Chunk 1 → AudioConversionContext → TranscribeChunk
   ├─→ Chunk 2 → AudioConversionContext → TranscribeChunk
   └─→ Chunk N → AudioConversionContext → TranscribeChunk
   ↓
4. Agregação dos resultados (ordenados)
   ↓
5. AnalyzerService.gerarResposta() → Gemini AI
   ↓
6. Resposta em streaming
```

### Componentes principais

#### TranscriptionsService
- **Responsabilidade:** Orquestrar o processo de transcrição
- **Padrões:** Usa Strategy, Factory e Adapter via injeção de dependência
- **Constantes configuráveis:**
  - `CHUNK_DURATION_SECONDS = 120` (2 minutos)
  - `TIMEOUT_MINUTES = 5L`
  - `AUDIO_CHUNK_SIZE = 8000`
  - `STREAM_DELAY_MS = 50L`

#### AudioConversionContext (Strategy)
- **Responsabilidade:** Selecionar estratégia de conversão apropriada
- **Funcionamento:** Spring injeta automaticamente todas as implementações de `AudioConversionStrategy`

#### SpeechConfigFactory (Factory)
- **Responsabilidade:** Criar configurações do Google Speech API
- **Métodos:**
  - `createRecognitionConfig()` - Configuração customizada
  - `createStreamingConfig()` - Configuração de streaming
  - `createDefaultStreamingConfig()` - Configuração padrão

#### FFmpegAdapter (Adapter)
- **Responsabilidade:** Encapsular operações do FFmpeg
- **Métodos:**
  - `getDuration()` - Obtém duração do arquivo
  - `splitFile()` - Divide arquivo em chunks
  - `convertToAudio()` - Converte para áudio WAV

## ⚙️ Configurações

### application.properties

```properties
# Nome da aplicação
spring.application.name=whisper-api-lab

# Tamanho máximo de upload
spring.servlet.multipart.max-file-size=500MB
spring.servlet.multipart.max-request-size=500MB

# Configuração do Gemini AI (carregado do .env)
spring.ai.google.genai.chat.options.model=gemini-2.0-flash
```

### Arquivo .env

```env
GOOGLE_APPLICATION_CREDENTIALS=seu-arquivo-credenciais.json
GEMINI_API_KEY=sua-api-key
```

## 🔧 Personalização

### Alterar duração dos chunks

Edite `TranscriptionsService.kt`:

```kotlin
companion object {
    private const val CHUNK_DURATION_SECONDS = 300 // 5 minutos
}
```

### Ajustar timeout

```kotlin
companion object {
    private const val TIMEOUT_MINUTES = 10L // 10 minutos
}
```

### Configurar idioma da transcrição

Edite `SpeechConfigFactory.kt`:

```kotlin
fun createRecognitionConfig(
    languageCode: String = "en-US", // Altere aqui
    // ...
)
```

### Adicionar nova estratégia de conversão

Crie uma nova classe:

```kotlin
@Component
class Mp3DirectStrategy : AudioConversionStrategy {
    override fun convert(file: MultipartFile): ByteArray {
        // Implementação específica
    }
    
    override fun supports(filename: String): Boolean {
        return filename.endsWith(".mp3", ignoreCase = true)
    }
}
```

O Spring automaticamente a incluirá no `AudioConversionContext`!

## 🧪 Testes

```bash
# Executar todos os testes
./gradlew test

# Executar com relatório detalhado
./gradlew test --info

# Executar testes específicos
./gradlew test --tests TranscriptionsServiceTest
```

## 📊 Performance

- **Processamento paralelo**: Reduz tempo total em até 80% para arquivos longos
- **Chunks de 2 minutos**: Balanceamento ideal entre velocidade e confiabilidade
- **Timeout configurável**: Previne travamentos em arquivos problemáticos
- **Conversão eficiente**: FFmpeg otimizado com arquivos temporários
- **Streaming de resposta**: Gemini AI retorna resultados progressivamente

## 🐛 Troubleshooting

### Erro: "FFmpeg not found"
```bash
# Verifique se FFmpeg está instalado
ffmpeg -version

# Adicione ao PATH se necessário
export PATH=$PATH:/caminho/para/ffmpeg
```

### Erro: "Arquivo .env não encontrado"
- Certifique-se de que o arquivo `.env` está na raiz do projeto
- Verifique as permissões de leitura do arquivo

### Erro: "Timeout ao processar chunk"
- Aumente o `TIMEOUT_MINUTES` em `TranscriptionsService.kt`
- Verifique sua conexão com a API do Google Cloud
- Reduza o `CHUNK_DURATION_SECONDS` para chunks menores

### Erro: "Formato de arquivo não suportado"
- Verifique se o formato está listado nas estratégias
- Adicione uma nova estratégia se necessário

### Erro: "GOOGLE_APPLICATION_CREDENTIALS not set"
- Verifique se o arquivo `.env` contém a variável
- Verifique se o caminho do arquivo JSON está correto
- Reinicie a aplicação após alterar o `.env`

## 📚 Documentação Adicional

- [DESIGN_PATTERNS.md](DESIGN_PATTERNS.md) - Detalhes dos design patterns implementados
- [SERVICE_ARCHITECTURE.md](SERVICE_ARCHITECTURE.md) - Arquitetura detalhada do service

## 🎯 Roadmap

- [ ] Adicionar cache de transcrições
- [ ] Implementar retry mechanism com backoff exponencial
- [ ] Suporte a múltiplos idiomas simultâneos
- [ ] Dashboard de monitoramento
- [ ] API de consulta de transcrições anteriores
- [ ] Suporte a webhooks para notificações
- [ ] Implementação de rate limiting

## 📝 Licença

Este projeto é um laboratório experimental para fins educacionais.

## 👥 Contribuindo

Contribuições são bem-vindas! Para contribuir:

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📧 Contato

Para dúvidas ou sugestões, abra uma issue no repositório.

---

**Nota**: Este projeto requer credenciais válidas do Google Cloud e pode gerar custos de uso das APIs Speech-to-Text e Gemini AI.

**Desenvolvido com ❤️ usando Kotlin e Spring Boot**
