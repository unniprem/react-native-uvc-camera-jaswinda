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
import kotlinx.coroutines.withContext

class UVCCameraViewModule(reactContext: ReactApplicationContext?) :
  ReactContextBaseJavaModule(reactContext) {
  private val coroutineScope = CoroutineScope(Dispatchers.Main)

  override fun getName() = TAG

  private suspend fun findCameraView(viewId: Int): UVCCameraView = withContext(Dispatchers.Main) {
    Log.d(TAG, "Finding UVCCameraView with id: $viewId")
    
    val uiManager = UIManagerHelper.getUIManager(reactApplicationContext, viewId)
    Log.d(TAG, "Finding UVCCameraView with id: $uiManager")
    
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

    return@withContext view ?: throw ViewNotFoundError(viewId).also {
      Log.e(TAG, "View with id $viewId is not a UVCCameraView")
    }
  }

  @ReactMethod
  fun openCamera(viewTag: Int) {
    coroutineScope.launch {
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
    coroutineScope.launch {
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
    coroutineScope.launch {
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
    coroutineScope.launch {
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
    coroutineScope.launch {
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
    coroutineScope.launch {
      withPromise(promise) {
        val view = findCameraView(viewTag)
        view.takePhoto()
      }
    }
  }

  @ReactMethod
  fun setDefaultCameraVendorId(viewTag: Int, vendorId: Int) {
    coroutineScope.launch {
      try {
        val view = findCameraView(viewTag)
        view.setDefaultCameraVendorId(vendorId)
      } catch (e: Exception) {
        Log.e(TAG, "Error setting default vendor ID: ${e.message}")
      }
    }
  }

  companion object {
    private const val TAG = "UVCCameraViewModule"
  }
}
