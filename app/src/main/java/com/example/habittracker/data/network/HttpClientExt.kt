package com.example.habittracker.data.network

import com.example.habittracker.domain.DataError
import com.example.habittracker.domain.Result
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.statement.HttpResponse
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext

suspend inline fun <reified T> safeCall(
    execute: () -> HttpResponse
): Result<T, DataError.Remote> {
    val response = try {
        execute()
    } catch (_: SocketTimeoutException) {
        return Result.Error(DataError.Remote.REQUEST_TIMEOUT)
    } catch (_: UnresolvedAddressException) {
        return Result.Error(DataError.Remote.NO_INTERNET)
    } catch (_: Exception) {
        coroutineContext.ensureActive()
        return Result.Error(DataError.Remote.UNKNOWN)
    }

    return responseToMyResult(response)
}

suspend inline fun <reified T> responseToMyResult(
    response: HttpResponse
): Result<T, DataError.Remote> = when (response.status.value) {
    in 200..299 -> {
        try {
            Result.Success(response.body<T>())
        } catch (_: NoTransformationFoundException) {
            Result.Error(DataError.Remote.SERIALIZATION)
        }
    }

    408 -> Result.Error(DataError.Remote.REQUEST_TIMEOUT)

    429 -> Result.Error(DataError.Remote.TOO_MANY_REQUESTS)

    in 500..599 -> Result.Error(DataError.Remote.SERVER)

    else -> Result.Error(DataError.Remote.UNKNOWN)
}