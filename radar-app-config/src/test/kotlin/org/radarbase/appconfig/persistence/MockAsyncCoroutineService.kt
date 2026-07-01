package org.radarbase.appconfig.persistence

import jakarta.ws.rs.container.AsyncResponse
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import org.radarbase.jersey.service.AsyncCoroutineService
import kotlin.time.Duration

class MockAsyncCoroutineService : AsyncCoroutineService {
    override fun <T> runAsCoroutine(
        asyncResponse: AsyncResponse,
        timeout: Duration,
        block: suspend () -> T,
    ) {
        runBlocking {
            try {
                val result = block()
                asyncResponse.resume(result)
            } catch (ex: Throwable) {
                asyncResponse.resume(ex)
            }
        }
    }

    override fun <T> runBlocking(timeout: Duration, block: suspend () -> T): T =
        kotlinx.coroutines.runBlocking {
            block()
        }

    override suspend fun <T> runInRequestScope(block: () -> T): T = block()

    override suspend fun <T> suspendInRequestScope(block: (CancellableContinuation<T>) -> Unit): T =
        suspendCancellableCoroutine(block)

    override suspend fun <T> withContext(name: String, block: suspend () -> T): T = block()
}
