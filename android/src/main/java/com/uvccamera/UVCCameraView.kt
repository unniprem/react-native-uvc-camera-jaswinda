package com.uvccamera

import android.content.Context
import android.hardware.usb.UsbDevice
import android.util.Log
import android.view.SurfaceHolder
import android.widget.FrameLayout
import com.facebook.react.bridge.ReactContext
import com.herohan.uvcapp.CameraHelper
import com.herohan.uvcapp.ICameraHelper
import com.serenegiant.usb.Size
import com.serenegiant.usb.UVCCamera
import com.serenegiant.widget.AspectRatioSurfaceView
import android.widget.Toast
import com.serenegiant.usb.UVCControl
import com.serenegiant.opengl.renderer.MirrorMode
import android.hardware.usb.UsbManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build

const val TAG = "UVCCameraView"

class UVCCameraView(context: Context) : FrameLayout(context) {

  companion object {
    private const val DEBUG = true
    private const val ACTION_USB_PERMISSION = "com.uvccamera.USB_PERMISSION"
  }

  var mCameraHelper: ICameraHelper? = null
  private val mCameraViewMain: AspectRatioSurfaceView
  private val usbManager: UsbManager by lazy {
    context.getSystemService(Context.USB_SERVICE) as UsbManager
  }

  private val reactContext: ReactContext
    get() = context as ReactContext

  init {
    mCameraViewMain = AspectRatioSurfaceView(reactContext)
    mCameraViewMain.layoutParams =
      LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    mCameraViewMain.holder.addCallback(object : SurfaceHolder.Callback {
      override fun surfaceCreated(holder: SurfaceHolder) {
        Log.d(TAG, "surfaceCreated() called with: holder = $holder")
        mCameraHelper?.addSurface(holder.surface, false)
        initCameraHelper()
      }

      override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

      }

      override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.d(TAG, "surfaceDestroyed() called with: holder = $holder")
        mCameraHelper?.removeSurface(holder.surface)
        clearCameraHelper()
      }
    })
    addView(mCameraViewMain)

  }

  private val mStateListener: ICameraHelper.StateCallback = object : ICameraHelper.StateCallback {
    override fun onAttach(device: UsbDevice) {
      if (DEBUG) Log.v(TAG, "onAttach:")
      requestUsbPermission(device)
    }

    override fun onDeviceOpen(device: UsbDevice, isFirstOpen: Boolean) {
      if (DEBUG) Log.v(TAG, "onDeviceOpen:")
      mCameraHelper?.openCamera()
    }

    override fun onCameraOpen(device: UsbDevice) {
      if (DEBUG) Log.v(TAG, "onCameraOpen:")
      mCameraHelper?.run {
        val portraitSizeList = ArrayList<Size>()
        for (size in supportedSizeList) {
          // if (size.width < size.height) {
            portraitSizeList.add(size)
           // }
        }
        Log.d(TAG, "portraitSizeList: $portraitSizeList")
        val size = portraitSizeList.last()
        //get the values from SharedPreferences
        val sharedPref = reactContext.getSharedPreferences("camera", Context.MODE_PRIVATE)

//         val width = sharedPref.getInt("width", 640)
//         val height = sharedPref.getInt("height", 480)
        // val width = sharedPref.getInt("width", 320)
        // val height = sharedPref.getInt("height", 240)
//        size.width = width;
//        size.height = height;
//        size.fps = 30;
        // Toast.makeText(reactContext, "rotate camera error: ${size.width}x${size.height},type:${size.type}, fps:${size.fps}", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "previewSize: $size")
        previewSize = size
        mCameraViewMain.setAspectRatio(size.width, size.height)
        var control: UVCControl = mCameraHelper!!.uvcControl
        control.zoomRelative = 500;

        startPreview()
        if(mCameraHelper!=null){
          try{
            mCameraHelper?.previewConfig = mCameraHelper?.previewConfig?.setRotation(360%360);

            if (deviceList != null && deviceList.isNotEmpty()) {
                var deviceToSelect: UsbDevice = deviceList[0]
                for (device in deviceList) {
                    val defaultCameraVendorId = sharedPref.getInt("defaultCameraVendorId", 3034)
                    if (device.vendorId == defaultCameraVendorId) {
                        deviceToSelect = device
                        break
                    }
                }
                if (deviceToSelect == null) {
                    // No device with vendor ID 3034 found, select the first available device
                    deviceToSelect = deviceList[0]

                }
                selectDevice(deviceToSelect)
            }
          } catch(e:Exception){
            // Toast.makeText(reactContext, "rotate camera error: $width, $height", Toast.LENGTH_SHORT).show()
          }
        }
        addSurface(mCameraViewMain.holder.surface, false)
      }
    }

    override fun onCameraClose(device: UsbDevice) {
      if (DEBUG) Log.v(TAG, "onCameraClose:")
      mCameraHelper?.removeSurface(mCameraViewMain.holder.surface)
    }

    override fun onDeviceClose(device: UsbDevice) {
      if (DEBUG) Log.v(TAG, "onDeviceClose:")
    }

    override fun onDetach(device: UsbDevice) {
      if (DEBUG) Log.v(TAG, "onDetach:")
    }

    override fun onCancel(device: UsbDevice) {
      if (DEBUG) Log.v(TAG, "onCancel:")
    }


  }

  private fun selectDevice(device: UsbDevice) {
    if (DEBUG) Log.v(TAG, "selectDevice:device=" + device.deviceName)
    mCameraHelper?.selectDevice(device)
  }

  private fun initCameraHelper() {
    if (DEBUG) Log.d(TAG, "initCameraHelper:")
    mCameraHelper = CameraHelper().apply {
      setStateCallback(mStateListener)
    }
  }

  private fun clearCameraHelper() {
    if (DEBUG) Log.d(TAG, "clearCameraHelper:")
    mCameraHelper?.release()
    mCameraHelper = null
  }

  fun openCamera() {
    mCameraHelper?.run {
      if (deviceList != null && deviceList.size > 0) {
        if( deviceList.size > 1) {
          selectDevice(deviceList[1])
        } else {
          selectDevice(deviceList[0])
        }
      }
    }

    // control.zoomAbsolute = 500;
    // control.focusAuto = true;
  }

  // fun openCamera() {
  //   mCameraHelper?.run {
  //     if (deviceList != null && deviceList.size > 0) {
  //       if( deviceList.size > 1) {
  //         selectDevice(deviceList[1])
  //       } else {
  //         selectDevice(deviceList[0])
  //       }
  //     }else{
  //       //use default camera
  //       selectDevice(null)
  //     }
  //   }
  // }

fun updateAspectRatio(width: Int, height: Int) {
    // Toast.makeText(reactContext, "$width X $height", Toast.LENGTH_SHORT).show();
   //set the values to SharedPreferences
    val sharedPref = reactContext.getSharedPreferences("camera", Context.MODE_PRIVATE) ?: return
    with (sharedPref.edit()) {
        putInt("width", width)
        putInt("height", height)
        commit()
      closeCamera()
      openCamera()
    }

}

  fun closeCamera() {
    mCameraHelper?.closeCamera()
  }

  fun  rotateCamera(){
    if(mCameraHelper!=null){
     try{
       mCameraHelper?.previewConfig = mCameraHelper?.previewConfig?.setMirror(MirrorMode.MIRROR_HORIZONTAL);
     } catch(e:Exception){
      //  Toast.makeText(reactContext, "rotate camera error", Toast.LENGTH_SHORT).show()
     }
    }
  }
  fun  setCameraBright(value:Int){
    if(mCameraHelper!=null){
      try {
        var control: UVCControl = mCameraHelper!!.uvcControl
        control.brightnessPercent = value;

    } catch(e:Exception){
      // Toast.makeText(reactContext, "Brightness error", Toast.LENGTH_SHORT).show()
    }
    }
  }
  //set DefaultCameraVendorId to shared preferences
  fun  setDefaultCameraVendorId(value:Int){
    //set the values to SharedPreferences
    val sharedPref = reactContext.getSharedPreferences("camera", Context.MODE_PRIVATE) ?: return
    with (sharedPref.edit()) {
        putInt("defaultCameraVendorId", value)
        commit()
    }
  }

  fun  setContast(value:Int) {
    if (mCameraHelper != null) {
      try {
        var control: UVCControl = mCameraHelper!!.uvcControl
        control.contrast = value;
      } catch (e: Exception) {
        // Toast.makeText(reactContext, "contrast error", Toast.LENGTH_SHORT).show()
      }
    }
  }
  fun  setHue(value:Int){
      if(mCameraHelper!=null){
        try {
          var control: UVCControl = mCameraHelper!!.uvcControl
          control.hue = value;
        } catch(e:Exception){
          // Toast.makeText(reactContext, "Hue error", Toast.LENGTH_SHORT).show()
        }
      }
  }
  fun  setSaturation(value:Int){
    if(mCameraHelper!=null){
      try {
        var control: UVCControl = mCameraHelper!!.uvcControl
        control.saturation = value;
      } catch(e:Exception){
        // Toast.makeText(reactContext, "saturation error", Toast.LENGTH_SHORT).show()
      }
    }
  }
  fun  setSharpness(value:Int){
    if(mCameraHelper!=null){
      try {
        var control: UVCControl = mCameraHelper!!.uvcControl
        control.saturation = value;
      } catch(e:Exception){
        // Toast.makeText(reactContext, "Sharpness error ", Toast.LENGTH_SHORT).show()
      }
    }
  }

  //setZoom
  fun  setZoom(value:Int){
    if(mCameraHelper!=null){
      try {
         var control: UVCControl = mCameraHelper!!.uvcControl
          control.zoomRelative = value;
          control.focusAuto = true;
        // mCameraHelper?.run {
        //   val control: UVCControl = uvcControl
        //   // control.zoomAbsolute = value;
        //   control.zoomRelative = value;
        //   // control.focusAuto = true;
        //   // startPreview()
        // }
        // Toast.makeText(reactContext, "Zoom value: $value", Toast.LENGTH_SHORT).show()
      } catch(e:Exception){
        // Toast.makeText(reactContext, "Zoom error", Toast.LENGTH_SHORT).show()
      }
    }
  }

  fun  reset(){
    if(mCameraHelper!=null){
      try {
        var control: UVCControl = mCameraHelper!!.uvcControl
        control.resetBrightness()
        control.resetContrast();
        control.resetHue();
        control.resetSaturation();
        control.resetSharpness();
      } catch(e:Exception){
        // Toast.makeText(reactContext, "reset error", Toast.LENGTH_SHORT).show()
      }
    }
  }

  private fun requestUsbPermission(device: UsbDevice) {
    if (usbManager.hasPermission(device)) {
      selectDevice(device)
      return
    }

    val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
    } else {
      PendingIntent.FLAG_UPDATE_CURRENT
    }

    val permissionIntent = PendingIntent.getBroadcast(
      context,
      0,
      Intent(ACTION_USB_PERMISSION),
      flags
    )

    // For Android 13 and above, we need to explicitly request the permission
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      try {
        usbManager.requestPermission(device, permissionIntent)
      } catch (e: Exception) {
        Log.e(TAG, "Error requesting USB permission: ${e.message}")
        // Fallback to direct selection if permission request fails
        selectDevice(device)
      }
    } else {
      selectDevice(device)
    }
  }
}
