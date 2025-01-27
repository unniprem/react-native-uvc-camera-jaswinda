package com.uvccamera

import android.util.Log
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.UiThreadUtil
import com.facebook.react.uimanager.UIManagerHelper
import com.uvccamera.utils.withPromise
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UVCCameraViewModule(reactContext: ReactApplicationContext?) :
  ReactContextBaseJavaModule(reactContext) {
  private val coroutineScope = CoroutineScope(Dispatchers.Main)

  override fun getName() = TAG

  private fun findCameraView(viewId: Int): UVCCameraView {
    Log.d(TAG, "Finding view $viewId...")
    val view = if (reactApplicationContext != null) {
      UIManagerHelper.getUIManager(reactApplicationContext, viewId)
        ?.resolveView(viewId) as UVCCameraView?
    } else null
    Log.d(
      TAG,
      if (reactApplicationContext != null) "Found view $viewId!" else "Couldn't find view $viewId!"
    )
    return view ?: throw ViewNotFoundError(viewId)
  }

  @ReactMethod
  fun openCamera(viewTag: Int) {
    UiThreadUtil.runOnUiThread {
      try {
        val view = findCameraView(viewTag)
        view.openCamera()
      } catch (e: Exception) {
        Log.e(TAG, "Error opening camera: ${e.message}")
      }
    }
  }

  @ReactMethod
  fun closeCamera(viewTag: Int) {
    UiThreadUtil.runOnUiThread {
      try {
        val view = findCameraView(viewTag)
        view.closeCamera()
      } catch (e: Exception) {
        Log.e(TAG, "Error closing camera: ${e.message}")
      }
    }
  }

  @ReactMethod
  fun updateAspectRatio(viewTag: Int, width: Int, height: Int) {
    UiThreadUtil.runOnUiThread {
      try {
        val view = findCameraView(viewTag)
        view.updateAspectRatio(width, height)
      } catch (e: Exception) {
        Log.e(TAG, "Error updating aspect ratio: ${e.message}")
      }
    }
  }

  @ReactMethod
  fun setCameraBright(viewTag: Int, brightness: Int) {
    UiThreadUtil.runOnUiThread {
      try {
        val view = findCameraView(viewTag)
        view.setCameraBright(brightness)
      } catch (e: Exception) {
        Log.e(TAG, "Error setting brightness: ${e.message}")
      }
    }
  }

  @ReactMethod
  fun setZoom(viewTag: Int, zoom: Int) {
    UiThreadUtil.runOnUiThread {
      try {
        val view = findCameraView(viewTag)
        view.setZoom(zoom)
      } catch (e: Exception) {
        Log.e(TAG, "Error setting zoom: ${e.message}")
      }
    }
  }

  @ReactMethod
  fun takePhoto(viewTag: Int, promise: Promise) {
    UiThreadUtil.runOnUiThread {
      coroutineScope.launch {
        withPromise(promise) {
          val view = findCameraView(viewTag)
          view.takePhoto()
        }
      }
    }
  }

  @ReactMethod
  fun setDefaultCameraVendorId(viewTag: Int, vendorId: Int) {
    UiThreadUtil.runOnUiThread {
      try {
        val view = findCameraView(viewTag)
        view.setDefaultCameraVendorId(vendorId)
      } catch (e: Exception) {
        Log.e(TAG, "Error setting default vendor ID: ${e.message}")
      }
    }
  }
}
