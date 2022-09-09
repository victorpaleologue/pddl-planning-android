package com.softbankrobotics.pddlplanning.utils

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Run the given function on the UI thread.
 */
fun postOnUiThread(block: () -> Unit) {
    Handler(Looper.getMainLooper()).post(block)
}

/**
 * Runs the given block in the UI thread, and waits synchronously for completion.
 */
fun <R> runBlockingOnUiThread(block: () -> R): R = runBlocking { onUiThread(block) }

/**
 * Transform a block into a function deferring it to the UI thread.
 */
suspend fun <R> onUiThread(block: () -> R): R {
    return suspendCoroutine {
        fun runBlockAndResume() {
            try {
                it.resume(block())
            } catch (t: Throwable) {
                it.resumeWithException(t)
            }
        }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runBlockAndResume()
        } else {
            postOnUiThread {
                runBlockAndResume()
            }
        }
    }
}

/**
 * Transform a block into a function deferring it to the UI thread.
 */
fun <R> onUiThreadAsync(block: () -> R): Deferred<R> {
    return createAsyncCoroutineScope().async { onUiThread(block) }
}
