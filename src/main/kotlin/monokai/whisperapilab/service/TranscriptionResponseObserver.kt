package monokai.whisperapilab.service

import com.google.api.gax.rpc.ApiStreamObserver
import com.google.cloud.speech.v1.StreamingRecognizeResponse
import java.util.concurrent.CountDownLatch

class TranscriptionResponseObserver(
    private val latch: CountDownLatch,
    private val resultBuilder: StringBuilder
) : ApiStreamObserver<StreamingRecognizeResponse> {
    
    override fun onNext(response: StreamingRecognizeResponse) {
        if (response.resultsCount > 0) {
            val result = response.resultsList[0]
            if (result.alternativesCount > 0) {
                val alternative = result.alternativesList[0]
                if (result.isFinal) {
                    resultBuilder.append(alternative.transcript).append(" ")
                }
            }
        }
    }

    override fun onError(t: Throwable) {
        println("Erro na transcrição: ${t.message}")
        latch.countDown()
    }

    override fun onCompleted() {
        latch.countDown()
    }
}
