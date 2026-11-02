package com.shilapi.xcertplay.usb

import android.app.Activity
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.shilapi.xcertplay.CarPlayHostActivity

/**
 * Trampoline Activity registered for USB_DEVICE_ATTACHED with usb_iphone_device_filter.xml.
 *
 * The static intent-filter is what Android requires to persist the system dialog's
 * "use by default for this USB device" checkbox and to auto-open DiPlay when the iPhone is
 * plugged in. CarPlayHostActivity itself carries only the meta-data (issue #311: a filter on
 * the heavy activity caused usbfs races on Allwinner T3 kernels); this translucent no-history
 * trampoline keeps that isolation while restoring plug-and-play on units like the W6.
 */
class IphoneUsbDeviceAttachedActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent?.action == UsbManager.ACTION_USB_DEVICE_ATTACHED) {
            val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(UsbManager.EXTRA_DEVICE) as? UsbDevice
            }
            Log.i(TAG, "iPhone USB attached: device=$device")
            startActivity(
                Intent(this, CarPlayHostActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT),
            )
        }
        finish()
    }

    private companion object {
        const val TAG = "IphoneUsbAttached"
    }
}
