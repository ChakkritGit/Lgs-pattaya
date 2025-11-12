package com.thanesgroup.lgs.screen.splashScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thanesgroup.lgs.ui.theme.LgsBlue
import kotlinx.coroutines.delay

@Composable
fun SplashScreen() {
  val fullText = "LGS"
  var displayedText by remember { mutableStateOf("") }
  var showIndicator by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    fullText.forEachIndexed { index, _ ->
      displayedText = fullText.take(index + 1)
      delay(400L)
    }
    delay(200L)
    showIndicator = true
  }

  Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      verticalArrangement = Arrangement.spacedBy(12.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = displayedText,
        fontSize = 52.sp,
        fontWeight = FontWeight.Bold,
        color = LgsBlue
      )

      if (showIndicator) {
        CircularProgressIndicator(
          modifier = Modifier.size(24.dp),
          color = LgsBlue,
          strokeWidth = 2.dp
        )
      }
    }
  }
}