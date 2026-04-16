package monokai.whisperapilab.service

import com.google.api.gax.rpc.ApiStreamObserver
import com.google.api.gax.rpc.BidiStreamingCallable
import com.google.cloud.speech.v1.StreamingRecognizeRequest
import com.google.cloud.speech.v1.StreamingRecognizeResponse
import com.google.cloud.speech.v1.StreamingRecognitionConfig
import com.google.protobuf.ByteString

class AudioStreamHandler(
    private val callable: BidiStreamingCallable<StreamingRecognizeRequest, StreamingRecognizeResponse>,
    private val responseObserver: ApiStreamObserver<StreamingRecognizeResponse>
) {
    private val requestObserver = callable.bidiStreamingCall(responseObserver)
    
    fun sendConfig(streamingConfig: StreamingRecognitionConfig) {
        val request = StreamingRecognizeRequest.newBuilder()
            .setStreamingConfig(streamingConfig)
            .build()
        requestObserver.onNext(request)
    }
    
    fun streamAudio(audioBytes: ByteArray, chunkSize: Int = 8000, delayMs: Long = 50) {
        var offset = 0
        while (offset < audioBytes.size) {
            val end = minOf(offset + chunkSize, audioBytes.size)
            val request = StreamingRecognizeRequest.newBuilder()
                .setAudioContent(ByteString.copyFrom(audioBytes, offset, end - offset))
                .build()
            requestObserver.onNext(request)
            offset = end
            Thread.sleep(delayMs)
        }
    }
    
    fun complete() {
        requestObserver.onCompleted()
    }
}
