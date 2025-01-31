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
    
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    
    override fun getName() = TAG

    private fun findCameraView(viewId: Int): UVCCameraView? {
        if (!UiThreadUtil.isOnUiThread()) {
            Log.e(TAG, "Not on UI thread")
            return null
        }
            
        Log.d(TAG, "Finding UVCCameraView with id: $viewId")
        
        val uiManager = UIManagerHelper.getUIManager(reactApplicationContext, viewId)
        if (uiManager == null) {
            Log.e(TAG, "Failed to get UIManager for viewId: $viewId")
            return null
        }
        
        return try {
            uiManager.resolveView(viewId) as? UVCCameraView
        } catch (e: Exception) {
            Log.e(TAG, "Error resolving view $viewId: ${e.message}")
            null
        }
    }

    @ReactMethod
    fun openCamera(viewTag: Int) {
        UiThreadUtil.runOnUiThread {
            try {
                findCameraView(viewTag)?.openCamera()
            } catch (e: Exception) {
                Log.e(TAG, "Error opening camera: ${e.message}")
            }
        }
    }

    @ReactMethod
    fun closeCamera(viewTag: Int) {
        UiThreadUtil.runOnUiThread {
            try {
                findCameraView(viewTag)?.closeCamera()
            } catch (e: Exception) {
                Log.e(TAG, "Error closing camera: ${e.message}")
            }
        }
    }

    @ReactMethod
    fun updateAspectRatio(viewTag: Int, width: Int, height: Int) {
        UiThreadUtil.runOnUiThread {
            try {
                findCameraView(viewTag)?.updateAspectRatio(width, height)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating aspect ratio: ${e.message}")
            }
        }
    }

    @ReactMethod
    fun setCameraBright(viewTag: Int, brightness: Int) {
        UiThreadUtil.runOnUiThread {
            try {
                findCameraView(viewTag)?.setCameraBright(brightness)
            } catch (e: Exception) {
                Log.e(TAG, "Error setting brightness: ${e.message}")
            }
        }
    }

    @ReactMethod
    fun setZoom(viewTag: Int, zoom: Int) {
        UiThreadUtil.runOnUiThread {
            try {
                findCameraView(viewTag)?.setZoom(zoom)
            } catch (e: Exception) {
                Log.e(TAG, "Error setting zoom: ${e.message}")
            }
        }
    }

    @ReactMethod
    fun takePhoto(viewTag: Int, promise: Promise) {
        UiThreadUtil.runOnUiThread {
            try {
                val photo = findCameraView(viewTag)?.takePhoto()
                promise.resolve(photo)
            } catch (e: Exception) {
                Log.e(TAG, "Error taking photo: ${e.message}")
                promise.reject("PHOTO_ERROR", e.message)
            }
        }
    }

    @ReactMethod
    fun setDefaultCameraVendorId(viewTag: Int, vendorId: Int) {
        UiThreadUtil.runOnUiThread {
            try {
                findCameraView(viewTag)?.setDefaultCameraVendorId(vendorId)
            } catch (e: Exception) {
                Log.e(TAG, "Error setting default vendor ID: ${e.message}")
            }
        }
    }

    companion object {
        private const val TAG = "UVCCameraViewModule"
    }
}