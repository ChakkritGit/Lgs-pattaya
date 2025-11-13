package com.thanesgroup.lgs.ui.component.menu

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.thanesgroup.lgs.R

@Composable
fun SettingsMenuItem(
  @DrawableRes icon: Int,
  text: String,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(horizontal = 16.dp, vertical = 16.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      painter = painterResource(icon),
      contentDescription = text,
      tint = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.width(16.dp))
    Text(
      text = text,
      style = MaterialTheme.typography.bodyLarge,
      modifier = Modifier.weight(1f)
    )
    Icon(
      painter = painterResource(R.drawable.arrow_forward_ios_24px),
      contentDescription = "Go to $text",
      tint = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}