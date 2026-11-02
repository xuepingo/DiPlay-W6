package com.shilapi.xcertplay.hud

import android.content.Context
import com.shilapi.xcertplay.transport.EvChargingConnectors

/**
 * W6 build: BYD navigation/HUD/cluster/vehicle outputs are removed. This stays as the plain
 * preference store the settings UI and call/media keys read; every capability probe answers
 * "not available" because the W6 has none of the BYD injection services.
 */
object BydOutputSettings {
    private const val PREFS = "diplay_byd_outputs"
    private const val KEY_ENABLED = "navigation_enabled"
    private const val KEY_CLUSTER_STREAM_PAUSE = "cluster_stream_pause"
    private const val KEY_BATTERY_TO_IPHONE = "battery_to_iphone"
    private const val KEY_LOW_CHARGE_PERCENT = "low_charge_percent"
    private const val KEY_CHARGING_CONNECTORS = "charging_connectors"
    private const val KEY_WHEEL_SPEED_TO_IPHONE = "wheel_speed_to_iphone"
    private const val KEY_VIDEO_WHILE_PARKED = "video_while_parked"
    private const val KEY_CLUSTER_SONG = "cluster_song"
    private const val KEY_CLUSTER_SONG_ON_CHANGE = "cluster_song_on_change"
    private const val KEY_HUD_SONG = "hud_song"
    private const val KEY_CARPLAY_CALLS = "carplay_calls"
    private const val KEY_CARPLAY_CALL_CONTROLS = "carplay_call_controls_experimental"
    private const val KEY_LEGACY_VEHICLE_PROBE = "legacy_vehicle_probe"
    const val DEFAULT_LOW_CHARGE_PERCENT = 20
    val lowChargePresets = listOf(10, 15, 20, 25, 30)

    fun enabled(context: Context): Boolean = prefs(context).getBoolean(KEY_ENABLED, true)
    fun setEnabled(context: Context, enabled: Boolean) = prefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply()

    fun clusterStreamPause(context: Context): Boolean = prefs(context).getBoolean(KEY_CLUSTER_STREAM_PAUSE, false)
    fun setClusterStreamPause(context: Context, enabled: Boolean) =
        prefs(context).edit().putBoolean(KEY_CLUSTER_STREAM_PAUSE, enabled).apply()

    fun batteryToIphone(context: Context): Boolean = prefs(context).getBoolean(KEY_BATTERY_TO_IPHONE, false)
    fun setBatteryToIphone(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_BATTERY_TO_IPHONE, enabled).commit()
    }

    /** Never active on the W6: the BYD vehicle-data sources are removed. */
    fun batteryToIphoneActive(context: Context): Boolean = false

    /** The charging inlets the iPhone is told about; applies on the next connection. */
    fun chargingConnectors(context: Context): EvChargingConnectors =
        prefs(context).getString(KEY_CHARGING_CONNECTORS, null)
            ?.let { saved -> EvChargingConnectors.entries.firstOrNull { it.name == saved } }
            ?: EvChargingConnectors.CCS2_TYPE2

    fun setChargingConnectors(context: Context, connectors: EvChargingConnectors) {
        prefs(context).edit().putString(KEY_CHARGING_CONNECTORS, connectors.name).commit()
    }

    fun wheelSpeedToIphone(context: Context): Boolean = prefs(context).getBoolean(KEY_WHEEL_SPEED_TO_IPHONE, false)
    fun setWheelSpeedToIphone(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_WHEEL_SPEED_TO_IPHONE, enabled).commit()
    }

    /** Never active on the W6: the BYD vehicle-data sources are removed. */
    fun wheelSpeedToIphoneActive(context: Context): Boolean = false

    fun videoWhileParked(context: Context): Boolean = prefs(context).getBoolean(KEY_VIDEO_WHILE_PARKED, false)
    fun setVideoWhileParked(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_VIDEO_WHILE_PARKED, enabled).commit()
    }

    /** Never active on the W6: it needs the BYD gear probe. */
    fun videoWhileParkedActive(context: Context): Boolean = false

    fun clusterSong(context: Context): Boolean = prefs(context).getBoolean(KEY_CLUSTER_SONG, false)
    fun setClusterSong(context: Context, enabled: Boolean) =
        prefs(context).edit().putBoolean(KEY_CLUSTER_SONG, enabled).apply()

    fun carPlayCalls(context: Context): Boolean = prefs(context).getBoolean(KEY_CARPLAY_CALLS, false)
    fun setCarPlayCalls(context: Context, enabled: Boolean) =
        prefs(context).edit().putBoolean(KEY_CARPLAY_CALLS, enabled).apply()

    fun carPlayCallControls(context: Context): Boolean =
        prefs(context).getBoolean(KEY_CARPLAY_CALL_CONTROLS, false)

    fun setCarPlayCallControls(context: Context, enabled: Boolean) =
        prefs(context).edit().putBoolean(KEY_CARPLAY_CALL_CONTROLS, enabled).apply()

    fun clusterSongOnChange(context: Context): Boolean = prefs(context).getBoolean(KEY_CLUSTER_SONG_ON_CHANGE, false)
    fun setClusterSongOnChange(context: Context, enabled: Boolean) =
        prefs(context).edit().putBoolean(KEY_CLUSTER_SONG_ON_CHANGE, enabled).apply()

    fun legacyVehicleProbe(context: Context): Boolean =
        prefs(context).getBoolean(KEY_LEGACY_VEHICLE_PROBE, false)

    fun setLegacyVehicleProbe(context: Context, enabled: Boolean) {
        check(prefs(context).edit().putBoolean(KEY_LEGACY_VEHICLE_PROBE, enabled).commit()) {
            "Could not persist vehicle-data mode"
        }
    }

    fun hudSong(context: Context): Boolean = prefs(context).getBoolean(KEY_HUD_SONG, false)
    fun setHudSong(context: Context, enabled: Boolean) =
        prefs(context).edit().putBoolean(KEY_HUD_SONG, enabled).apply()

    fun lowChargePercent(context: Context): Int = prefs(context).getInt(KEY_LOW_CHARGE_PERCENT, DEFAULT_LOW_CHARGE_PERCENT)
    fun setLowChargePercent(context: Context, percent: Int) =
        prefs(context).edit().putInt(KEY_LOW_CHARGE_PERCENT, percent).apply()

    /** W6: no BYD standalone HUD. */
    fun standaloneHudAvailable(context: Context): Boolean = false
    fun standaloneHudDiagnosticReport(context: Context): String = ""

    /** W6: no BYD navigation receiver. */
    fun navigationAvailable(context: Context): Boolean = false

    /** W6: not a BYD head unit. */
    fun available(context: Context): Boolean = false

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
