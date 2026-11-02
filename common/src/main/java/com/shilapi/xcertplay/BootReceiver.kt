package com.shilapi.xcertplay

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import com.shilapi.xcertplay.network.CarHotspotSettings
import com.shilapi.xcertplay.network.CarHotspotStatus
import com.shilapi.xcertplay.network.CarHotspotTethering
import com.shilapi.xcertplay.network.WirelessStartupPolicy
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

/** Starts the CarPlay host after boot when the user has enabled the startup option. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != Intent.ACTION_BOOT_COMPLETED && action != ACTION_QUICKBOOT_POWERON) return
        // Qualcomm QVM units broadcast QUICKBOOT_POWERON and may add BOOT_COMPLETED right
        // after it: process one broadcast of such a mixed pair (M11). Repeated broadcasts of
        // the SAME action are real later boots — each one runs again and replaces the
        // recorded startup evidence.
        val now = SystemClock.elapsedRealtime()
        val previousAction = lastHandledAction.get()
        val previousAt = lastHandledElapsed.get()
        if (previousAction != null && previousAction != action &&
            previousAt != 0L && now - previousAt < DEDUPE_WINDOW_MILLIS
        ) {
            Log.i(TAG, "Boot broadcast $action ignored: paired $previousAction handled ${now - previousAt}ms ago")
            return
        }
        lastHandledAction.set(action)
        lastHandledElapsed.set(now)
        val launchEnabled = AirPlayPersistence.loadAutoStartOnBoot(context)
        StartupDiagnosticSnapshot.received(context, launchEnabled)
        if (launchEnabled) {
            val launch = Intent(context, DiPlayActivity::class.java).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP,
                )
            }
            try {
                context.startActivity(launch)
                StartupDiagnosticSnapshot.launchResult(context)
            } catch (error: RuntimeException) {
                StartupDiagnosticSnapshot.launchResult(context, error)
                Log.w(TAG, "Boot auto-start could not launch DiPlayActivity", error)
            }
        }

        healHotspotIfOptedIn(context)
    }

    /**
     * Issue #315 follow-up: boot-time hotspot self-healing. Some head units
     * (e.g. Zhangxun PF8227) never bring their own hotspot up after boot, so
     * the phone has no AP to join and wireless CarPlay cannot start until
     * the driver opens system settings by hand. When the user has opted into
     * automatic hotspot startup (the same gate as the launch-time path —
     * auto_enable on, wireless on, manual mode, valid saved SSID/passphrase;
     * there is no session at boot), bring the AP up here, independent of
     * whether the DiPlay UI itself auto-launches. The enable call blocks
     * (up to the boot hotspot budget) so it runs on a worker thread behind
     * goAsync; failures only log — the per-connection auto-enable path
     * remains the fallback, and nothing is ever prompted for at boot.
     */
    private fun healHotspotIfOptedIn(context: Context) {
        if (!CarHotspotSetup.shouldStartOnLaunch(context, hasSession = false)) return
        if (CarHotspotStatus.isEnabled(context) == true) {
            Log.i(TAG, "Boot hotspot self-heal: hotspot already on, nothing to do")
            return
        }
        val pending = goAsync()
        Thread({
            try {
                val result = CarHotspotTethering.enable(
                    context,
                    isCancelled = { !CarHotspotSettings.enabled(context) },
                    timeoutMillis = WirelessStartupPolicy.BOOT_HOTSPOT_READY_MILLIS,
                    log = { Log.i(TAG, "Boot hotspot self-heal: $it") },
                )
                Log.i(TAG, "Boot hotspot self-heal finished: $result")
            } catch (error: Throwable) {
                Log.w(TAG, "Boot hotspot self-heal failed", error)
            } finally {
                pending.finish()
            }
        }, "boot-hotspot-heal").start()
    }

    private companion object {
        const val TAG = "xcertplay-boot"
        const val ACTION_QUICKBOOT_POWERON = "android.intent.action.QUICKBOOT_POWERON"
        /** QUICKBOOT_POWERON and BOOT_COMPLETED arrive within seconds of each other. */
        const val DEDUPE_WINDOW_MILLIS = 30_000L
        val lastHandledAction = AtomicReference<String?>(null)
        val lastHandledElapsed = AtomicLong(0L)
    }
}
