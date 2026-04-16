# Whisper API Lab

API REST para transcrição de áudio e vídeo usando Google Cloud Speech-to-Text com processamento paralelo e integração com Google Gemini AI.

## 📋 Descrição

Este projeto oferece uma solução robusta para transcrição de arquivos de áudio e vídeo longos, dividindo-os automaticamente em chunks de até 5 minutos e processando-os em paralelo usando Kotlin Coroutines. A API também integra o Google Gemini AI para análise e processamento de texto.

## ✨ Funcionalidades

- 🎵 **Transcrição de áudio/vídeo**: Suporte para múltiplos formatos (WAV, MP4, MP3, etc.)
- ⚡ **Processamento paralelo**: Usa Kotlin Coroutines para processar múltiplos chunks simultaneamente
- 📦 **Divisão automática**: Arquivos longos são divididos em partes de até 5 minutos
- 🔄 **Conversão automática**: Converte vídeos para áudio usando FFmpeg
- 🤖 **Integração com Gemini AI**: Análise e processamento de texto com Google Gemini
- 📤 **Upload de arquivos grandes**: Suporta arquivos de até 500MB

## 🛠️ Tecnologias

- **Kotlin** 2.2.21
- **Spring Boot** 4.0.5
- **Google Cloud Speech-to-Text** 4.78.0
- **Google Gemini AI** (Spring AI 1.1.0)
- **Kotlin Coroutines** 1.10.0
- **FFmpeg** (para processamento de áudio/vídeo)
- **Gradle** 9.4.1

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
   - Configure a variável de ambiente `GOOGLE_APPLICATION_CREDENTIALS`

2. **Google Gemini API**
   - Obtenha uma API Key em [Google AI Studio](https://makersuite.google.com/app/apikey)
   - Configure a variável de ambiente `GEMINI_API_KEY`

## 🚀 Instalação e Configuração

### 1. Clone o repositório

```bash
git clone https://github.com/seu-usuario/whisper-api-lab.git
cd whisper-api-lab
```

### 2. Configure as variáveis de ambiente

Crie um arquivo `.env` na raiz do projeto:

```bash
# Google Cloud Speech-to-Text
GOOGLE_APPLICATION_CREDENTIALS=/caminho/para/seu/arquivo-credenciais.json

# Google Gemini AI
GEMINI_API_KEY=sua-api-key-aqui
```

### 3. Execute o projeto

```bash
# Linux/macOS
./gradlew bootRun

# Windows
gradlew.bat bootRun
```

A aplicação estará disponível em `http://localhost:8080`

## 📡 Endpoints da API

### POST /whisper

Transcreve um arquivo de áudio ou vídeo.

**Request:**
```bash
curl -X POST http://localhost:8080/whisper \
  -F "file=@/caminho/para/seu/arquivo.mp4"
```

**Response:**
```text
Texto transcrito do áudio/vídeo...
```

**Formatos suportados:**
- Áudio: WAV, MP3, M4A, FLAC, OGG
- Vídeo: MP4, AVI, MOV, MKV, WEBM

**Características:**
- Arquivos são automaticamente divididos em chunks de 5 minutos
- Processamento paralelo para maior velocidade
- Conversão automática para formato WAV 16kHz mono
- Timeout de 5 minutos por chunk

## 🏗️ Arquitetura

```
whisper-api-lab/
├── controller/
│   └── WhisperAPIController.kt      # Endpoints REST
├── service/
│   ├── TranscriptionsService.kt     # Lógica de transcrição
│   └── AnalyzerService.kt           # Integração com Gemini AI
├── utils/
│   ├── AudioSplitterUtils.kt        # Divisão de arquivos
│   └── VideoToAudioUtils.kt         # Conversão de vídeo
└── model/
    ├── Response.kt                   # Modelos de resposta
    └── Usage.kt
```

### Fluxo de processamento

1. **Upload**: Arquivo é recebido via endpoint `/whisper`
2. **Análise**: FFprobe verifica a duração do arquivo
3. **Divisão**: Se > 5 minutos, divide em chunks usando FFmpeg
4. **Conversão**: Converte para WAV 16kHz mono (se necessário)
5. **Transcrição paralela**: Processa todos os chunks simultaneamente
6. **Agregação**: Concatena resultados na ordem correta
7. **Resposta**: Retorna texto completo transcrito

## ⚙️ Configurações

Edite `src/main/resources/application.properties`:

```properties
# Nome da aplicação
spring.application.name=whisper-api-lab

# Tamanho máximo de upload
spring.servlet.multipart.max-file-size=500MB
spring.servlet.multipart.max-request-size=500MB

# Configuração do Gemini AI
spring.ai.google.genai.chat.options.model=gemini-2.0-flash
spring.ai.google.genai.api-key=${GEMINI_API_KEY}
```

## 🔧 Personalização

### Alterar duração dos chunks

Edite `TranscriptionsService.kt`:

```kotlin
fun transcription(arquivo: MultipartFile): String = runBlocking {
    val chunks = splitAudioIntoChunks(arquivo, 300) // 300 segundos = 5 minutos
    // ...
}
```

### Ajustar timeout

Edite `TranscriptionsService.kt`:

```kotlin
if (!latch.await(5, TimeUnit.MINUTES)) { // Altere aqui
    throw RuntimeException("Timeout ao processar chunk de áudio")
}
```

### Configurar idioma da transcrição

Edite `TranscriptionsService.kt`:

```kotlin
val config = RecognitionConfig.newBuilder()
    .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
    .setSampleRateHertz(16000)
    .setLanguageCode("pt-BR") // Altere aqui (en-US, es-ES, etc.)
    .build()
```

## 🧪 Testes

```bash
# Executar todos os testes
./gradlew test

# Executar com relatório detalhado
./gradlew test --info
```

## 📊 Performance

- **Processamento paralelo**: Reduz tempo total em até 80% para arquivos longos
- **Chunks de 5 minutos**: Balanceamento ideal entre velocidade e confiabilidade
- **Timeout configurável**: Previne travamentos em arquivos problemáticos
- **Conversão eficiente**: FFmpeg otimizado para processamento em memória

## 🐛 Troubleshooting

### Erro: "FFmpeg not found"
```bash
# Verifique se FFmpeg está instalado
ffmpeg -version

# Adicione ao PATH se necessário
export PATH=$PATH:/caminho/para/ffmpeg
```

### Erro: "Pipe quebrado"
- Certifique-se de que está usando a versão mais recente do código
- Verifique se há espaço em disco suficiente para arquivos temporários

### Erro: "Timeout ao processar chunk"
- Aumente o timeout em `TranscriptionsService.kt`
- Verifique sua conexão com a API do Google Cloud

### Erro: "GOOGLE_APPLICATION_CREDENTIALS not set"
```bash
export GOOGLE_APPLICATION_CREDENTIALS="/caminho/para/credenciais.json"
```

## 📝 Licença

Este projeto é um laboratório experimental para fins educacionais.

## 👥 Contribuindo

Contribuições são bem-vindas! Sinta-se à vontade para abrir issues ou pull requests.

## 📧 Contato

Para dúvidas ou sugestões, abra uma issue no repositório.

---

**Nota**: Este projeto requer credenciais válidas do Google Cloud e pode gerar custos de uso da API Speech-to-Text.
