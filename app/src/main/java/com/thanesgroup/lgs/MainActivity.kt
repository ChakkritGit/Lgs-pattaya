package com.thanesgroup.lgs

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.thanesgroup.lgs.navigation.AppNavigation
import com.thanesgroup.lgs.ui.theme.LGSTheme
import java.io.File

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    installSplashScreen()
    super.onCreate(savedInstanceState)
    cleanupUpdateFile()
    enableEdgeToEdge()
    setContent {
      LGSTheme {
        val navController = rememberNavController()

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          AppNavigation(innerPadding = innerPadding, navController = navController, context = this)
        }
      }
    }
  }

  private fun cleanupUpdateFile() {
    val updateFile = File(externalCacheDir, "update.apk")

    if (updateFile.exists()) {
      try {
        if (updateFile.delete()) {
          Log.d("UpdateCleanup", "ไฟล์ update.apk ที่ค้างอยู่ถูกลบแล้ว")
        } else {
          Log.e("UpdateCleanup", "ไม่สามารถลบไฟล์ update.apk ได้")
        }
      } catch (e: SecurityException) {
        Log.e("UpdateCleanup", "ไม่มีสิทธิ์ในการลบไฟล์: ${e.message}")
      }
    } else {
      Log.d("UpdateCleanup", "ไม่พบไฟล์ update.apk ให้ลบ")
    }
  }
}
