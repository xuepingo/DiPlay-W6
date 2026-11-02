package com.shilapi.xcertplay.hud

import android.content.Context
import com.shilapi.xcertplay.iap2.wire.Iap2Frame

/** What the car shows for a CarPlay call: the phase and the caller, and since when it is connected. */
data class CarPlayCallCard(val phase: Phase, val name: String, val activeSinceMillis: Long? = null) {
    enum class Phase { RINGING, DIALING, ACTIVE }
}

/**
 * W6 build: the BYD navigation/HUD/cluster/vehicle outputs are removed (the W6's instrument is
 * driven by its own chip and has no injectable protocol). The object stays as the no-op boundary
 * the phone/control layers already call into; steering-wheel call keys simply see no call card.
 */
object BydNavigationOutputs {
    fun onAppOpened(context: Context) {}
    fun setDiagnosticHold(hold: Boolean) {}
    fun setClusterMapShown(shown: Boolean) {}
    fun setClusterStreamControl(control: (Boolean) -> Unit) {}
    fun clearClusterStreamControl(control: (Boolean) -> Unit) {}
    fun start(context: Context) {}
    internal fun onFrame(frame: Iap2Frame) {}
    fun setTurnOverlayListener(listener: ((ClusterTurnGuidance?) -> Unit)?) {}
    fun refreshTurnOverlay() {}
    fun clusterSongChanged(enabled: Boolean) {}
    fun carPlayCallsChanged(enabled: Boolean) {}
    fun carPlaySessionStarted() {}
    fun carPlayCall(): CarPlayCallCard? = null
    fun clusterSongOnChangeChanged() {}
    fun dashboardNote(text: String, source: Int? = null) {}
    fun endNow(preserveTurnOverlay: Boolean = false) {}

    /** No vehicle telemetry on the W6 (the BYD adb sources are gone). */
    fun batteryStatus(context: Context): com.shilapi.xcertplay.transport.VehicleStatusProvider =
        com.shilapi.xcertplay.transport.VehicleStatusProvider { null }

    fun wheelSpeed(context: Context): com.shilapi.xcertplay.transport.VehicleSpeedSource =
        object : com.shilapi.xcertplay.transport.VehicleSpeedSource {
            override fun start() {}
            override fun stop() {}
            override fun drain(): com.shilapi.xcertplay.transport.VehicleSpeedReading? = null
        }

    fun parked(context: Context): Boolean? = null
}
