package com.thanesgroup.lgs.util

import android.app.Activity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowInsetsControllerCompat

fun updateStatusBarColor(activity: Activity, color: Color) {
  val window = activity.window
  window.statusBarColor = color.toArgb()

  WindowInsetsControllerCompat(window, window.decorView)
    .isAppearanceLightStatusBars = color.luminance() > 0.5f
}