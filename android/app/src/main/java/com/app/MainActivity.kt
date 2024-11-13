package com.seniorbuddy.abby

import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import com.facebook.react.ReactActivity
import com.facebook.react.ReactActivityDelegate
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint.fabricEnabled
import com.facebook.react.defaults.DefaultReactActivityDelegate

class MainActivity : ReactActivity() {

  override fun getMainComponentName(): String = "app"

  override fun createReactActivityDelegate(): ReactActivityDelegate =
      DefaultReactActivityDelegate(this, mainComponentName, fabricEnabled)

  override fun onResume() {
    super.onResume()
    // Overlay 권한 요청
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if (!Settings.canDrawOverlays(this)) {
        Toast.makeText(this, "Overlay 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, 
            android.net.Uri.parse("package:$packageName"))
        startActivity(intent)
      }
    }
  }
}