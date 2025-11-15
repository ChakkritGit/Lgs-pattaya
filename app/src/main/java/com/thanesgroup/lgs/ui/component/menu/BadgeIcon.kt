package com.thanesgroup.lgs.ui.component.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp

@Composable
fun BadgedIcon(
  modifier: Modifier = Modifier,
  painter: Painter,
  contentDescription: String,
  showBadge: Boolean,
  tint: Color
) {
  Box(modifier) {
    Icon(
      painter = painter,
      contentDescription = contentDescription,
      modifier = Modifier.size(28.dp),
      tint = tint
    )

    if (showBadge) {
      Box(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .offset(x = 3.dp, y = (-3).dp)
          .size(8.dp)
          .clip(CircleShape)
          .background(Color.Red)
      )
    }
  }
}