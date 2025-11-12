package com.thanesgroup.lgs.ui.component.keyboard

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

class Keyboard {

  companion object {
    @Composable
    fun hideKeyboard(): () -> Unit {
      val keyboardController = LocalSoftwareKeyboardController.current
      return {
        keyboardController?.hide()
      }
    }
  }
}