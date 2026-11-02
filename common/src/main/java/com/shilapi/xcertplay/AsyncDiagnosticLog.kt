package com.shilapi.xcertplay

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Queued entries retain only their exact file target and a redacted, bounded metadata string. */
internal object AsyncDiagnosticLog {
    private data class Entry(val target: SessionLogFile, val line: String)
    private val writer = BoundedDiagnosticWriter<Entry> { it.target.append(it.line) }

    /**
     * Process-global session log so services outside [CarPlayHostActivity] can write
     * into the same diplay.log the diagnostic report exports.
     */
    @Volatile
    var defaultTarget: SessionLogFile? = null

    fun append(target: SessionLogFile?, message: String, nowMillis: Long = System.currentTimeMillis()) {
        if (target == null) return
        val safe = DiagnosticRedactor.redact(message) ?: return
        val timestamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date(nowMillis))
        runCatching { writer.enqueue(Entry(target, "$timestamp  $safe")) }
    }

    /** Write to the process-global session log (no-op before the host activity initialises it). */
    fun log(message: String, nowMillis: Long = System.currentTimeMillis()) =
        append(defaultTarget, message, nowMillis)

    internal fun awaitIdle(timeoutMillis: Long): Boolean = writer.awaitIdle(timeoutMillis)
}
