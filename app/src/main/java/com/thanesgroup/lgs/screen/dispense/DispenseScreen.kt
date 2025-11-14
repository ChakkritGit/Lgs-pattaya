package com.thanesgroup.lgs.screen.dispense

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.thanesgroup.lgs.R
import com.thanesgroup.lgs.ui.component.BarcodeScanner
import com.thanesgroup.lgs.ui.theme.LgsBlue

@Composable
fun DispenseScreen(
  navController: NavHostController,
  context: Context
) {
  var scannedText by remember { mutableStateOf("") }

  BarcodeScanner { scannedCode ->
    scannedText = scannedCode
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 24.dp, vertical = 52.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    Text(
      text = "Light Guiding Station",
      fontSize = 32.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onBackground
    )
    Text(
      text = "( LGS )",
      fontSize = 28.sp,
      color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
      text = "Scan Barcode / QRCode",
      fontSize = 20.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(24.dp))

    Box(
      modifier = Modifier
        .size(220.dp)
        .clip(CircleShape)
        .padding(4.dp)
        .border(BorderStroke(4.dp, LgsBlue), CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        painter = painterResource(id = R.drawable.lgs_scan),
        contentDescription = "Scan Barcode or QRCode",
        modifier = Modifier.size(120.dp),
        tint = MaterialTheme.colorScheme.onSurface
      )
    }
    Spacer(modifier = Modifier.height(24.dp))

    Text(
      text = "สแกน hn",
      fontSize = 24.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      text = "จัดยาตามรายการยาในใบสั่งยาที่ทำการระบุ",
      fontSize = 16.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(18.dp))
    Text(scannedText, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LgsBlue)
  }
}