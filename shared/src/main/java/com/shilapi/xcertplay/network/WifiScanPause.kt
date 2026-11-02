package com.shilapi.xcertplay.network

import android.content.Context
import android.os.Build

/**
 * W6 build: the BYD DiLink 3 Wi-Fi scan pause (local adb shell) is removed. Kept as a no-op
 * so the wireless controller's lease bookkeeping compiles unchanged; nothing is ever paused.
 */
internal class WifiScanPause(
    context: Context,
    private val log: (String) -> Unit,
) {
    fun pause() {}
    fun close() {}

    internal companion object {
        fun eligible(backend: WirelessHotspotBackend, sdk: Int = Build.VERSION.SDK_INT): Boolean = false

        fun restoreIfNeeded(context: Context) {}
    }
}
