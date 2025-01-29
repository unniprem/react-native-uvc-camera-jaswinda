package com.uvccamera

import android.app.Activity
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.util.Log

class UVCPermissionActivity : Activity() {
    companion object {
        private const val TAG = "UVCPermissionActivity"
        const val EXTRA_DEVICE = "device"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val device: UsbDevice? = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_DEVICE, UsbDevice::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_DEVICE)
        }

        if (device == null) {
            finish()
            return
        }

        val usbManager = getSystemService(USB_SERVICE) as UsbManager
        if (!usbManager.hasPermission(device)) {
            val intent = Intent(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            intent.putExtra(UsbManager.EXTRA_DEVICE, device)
            intent.putExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
            sendBroadcast(intent)
        }

        finish()
    }
}
