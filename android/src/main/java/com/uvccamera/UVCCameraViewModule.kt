package com.uvccamera

import android.util.Log
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
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
    Log.d(TAG, "Finding UVCCameraView with id: $viewId")
    
    val uiManager = UIManagerHelper.getUIManager(reactApplicationContext, viewId)
    if (uiManager == null) {
      Log.e(TAG, "Failed to get UIManager for viewId: $viewId")
      throw ViewNotFoundError(viewId)
    }

    val view = try {
      uiManager.resolveView(viewId) as? UVCCameraView
    } catch (e: Exception) {
      Log.e(TAG, "Error resolving view $viewId: ${e.message}")
      null
    }

    return view ?: throw ViewNotFoundError(viewId).also {
      Log.e(TAG, "View with id $viewId is not a UVCCameraView")
    }
  }


  @ReactMethod
  fun openCamera(viewTag: Int) {
    val view = findCameraView(viewTag)
    view.openCamera()
  }

  @ReactMethod
  fun closeCamera(viewTag: Int) {
    val view = findCameraView(viewTag)
    view.closeCamera()
  }

  @ReactMethod
  fun updateAspectRatio(viewTag: Int, width: Int, height: Int) {
    val view = findCameraView(viewTag)
    view.updateAspectRatio(width, height)
  }

  @ReactMethod
  fun setCameraBright(viewTag: Int, brightness: Int) {
    val view = findCameraView(viewTag)
    view.setCameraBright(brightness)
  }

  @ReactMethod
  fun setZoom(viewTag: Int, zoom: Int) {
    val view = findCameraView(viewTag)
    view.setZoom(zoom)
  }


  @ReactMethod
  fun takePhoto(viewTag: Int, promise: Promise) {
    coroutineScope.launch {
      withPromise(promise) {
        val view = findCameraView(viewTag)
        view.takePhoto()
      }
    }
  }

  @ReactMethod
  fun setDefaultCameraVendorId(viewTag: Int, vendorId: Int) {
    val view = findCameraView(viewTag)
    view.setDefaultCameraVendorId(vendorId)
  }
}
