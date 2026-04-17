# Arquitetura do TranscriptionsService

## Visão Geral

O `TranscriptionsService` foi completamente reestruturado para aplicar design patterns e princípios SOLID, resultando em código mais limpo, testável e manutenível.

## Estrutura de Classes

```
TranscriptionsService
├── Dependencies (Injeção de Dependência)
│   ├── MediaProcessor (Adapter Pattern)
│   ├── SpeechConfigFactory (Factory Pattern)
│   └── AudioConversionContext (Strategy Pattern)
│
└── Helper Classes
    ├── TranscriptionResponseObserver (Observer Pattern)
    └── AudioStreamHandler (Encapsulamento)
```

## Componentes

### 1. TranscriptionsService (Orquestrador)

**Responsabilidade:** Coordenar o processo de transcrição

**Métodos principais:**
- `transcription(arquivo)` - Processa arquivo completo com paralelismo
- `transcribeChunk(arquivo)` - Transcreve um único chunk

**Constantes configuráveis:**
```kotlin
CHUNK_DURATION_SECONDS = 120    // Duração de cada parte
TIMEOUT_MINUTES = 5L            // Timeout por chunk
AUDIO_CHUNK_SIZE = 8000         // Tamanho do buffer de streaming
STREAM_DELAY_MS = 50L           // Delay entre envios
```

### 2. TranscriptionResponseObserver

**Responsabilidade:** Processar respostas do Google Speech API

**Padrão:** Observer Pattern

**Características:**
- Classe reutilizável e testável
- Separação de responsabilidades
- Tratamento de erros centralizado

### 3. AudioStreamHandler

**Responsabilidade:** Gerenciar streaming de áudio para API

**Características:**
- Encapsula lógica de streaming
- Interface limpa e simples
- Configurável (chunk size, delay)

## Fluxo de Execução

```
1. transcription(arquivo)
   ↓
2. mediaProcessor.splitFile() → [chunk1, chunk2, ...]
   ↓
3. async { transcribeChunk(chunk) } (paralelo)
   ↓
4. Para cada chunk:
   a. audioConversionContext.convertAudio()
   b. speechConfigFactory.createDefaultStreamingConfig()
   c. TranscriptionResponseObserver (observer)
   d. AudioStreamHandler (streaming)
   e. Aguarda resultado com timeout
   ↓
5. Agrega resultados ordenados
   ↓
6. Retorna transcrição completa
```

## Design Patterns Aplicados

### Strategy Pattern
```kotlin
// Conversão automática baseada no formato
val audioBytes = audioConversionContext.convertAudio(arquivo)
```

### Factory Pattern
```kotlin
// Criação de configurações padronizadas
val streamingConfig = speechConfigFactory.createDefaultStreamingConfig()
```

### Adapter Pattern
```kotlin
// Interface unificada para FFmpeg
val chunks = mediaProcessor.splitFile(arquivo, CHUNK_DURATION_SECONDS)
```

### Observer Pattern
```kotlin
// Observador de respostas da API
val responseObserver = TranscriptionResponseObserver(latch, resultBuilder)
```

## Benefícios da Refatoração

### 1. Testabilidade
- Cada componente pode ser testado isoladamente
- Fácil criar mocks das dependências
- Observer e StreamHandler são testáveis unitariamente

### 2. Manutenibilidade
- Código organizado e bem estruturado
- Responsabilidades claras
- Constantes configuráveis centralizadas

### 3. Extensibilidade
- Fácil adicionar novos formatos (Strategy)
- Fácil mudar configurações (Factory)
- Fácil substituir FFmpeg (Adapter)

### 4. Legibilidade
- Método `transcribeChunk` reduzido de ~60 para ~15 linhas
- Lógica complexa encapsulada em classes auxiliares
- Nomes descritivos e intenção clara

### 5. SOLID Principles

**Single Responsibility:**
- TranscriptionsService: orquestração
- TranscriptionResponseObserver: processar respostas
- AudioStreamHandler: gerenciar streaming

**Open/Closed:**
- Aberto para extensão (novos strategies, adapters)
- Fechado para modificação (interfaces estáveis)

**Liskov Substitution:**
- Qualquer implementação de MediaProcessor funciona
- Qualquer AudioConversionStrategy funciona

**Interface Segregation:**
- Interfaces pequenas e focadas
- Clientes dependem apenas do que usam

**Dependency Inversion:**
- Service depende de abstrações (interfaces)
- Não depende de implementações concretas

## Exemplo de Teste

```kotlin
@Test
fun `deve transcrever chunk com sucesso`() {
    // Arrange
    val mockMediaProcessor = mock<MediaProcessor>()
    val mockFactory = mock<SpeechConfigFactory>()
    val mockContext = mock<AudioConversionContext>()
    
    val service = TranscriptionsService(
        mockMediaProcessor,
        mockFactory,
        mockContext
    )
    
    // Act & Assert
    // ...
}
```

## Configuração Personalizada

Para alterar comportamento, basta modificar as constantes:

```kotlin
companion object {
    private const val CHUNK_DURATION_SECONDS = 300  // 5 minutos
    private const val TIMEOUT_MINUTES = 10L         // 10 minutos
    private const val AUDIO_CHUNK_SIZE = 16000      // Buffer maior
    private const val STREAM_DELAY_MS = 25L         // Mais rápido
}
```

## Próximos Passos

1. Adicionar logging estruturado
2. Implementar retry mechanism
3. Adicionar métricas de performance
4. Cache de transcrições
5. Suporte a múltiplos idiomas simultâneos
