package com.thanesgroup.lgs.screen.dispense

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thanesgroup.lgs.R
import com.thanesgroup.lgs.data.model.DispenseModel
import com.thanesgroup.lgs.data.model.OrderModel
import com.thanesgroup.lgs.data.viewModel.DispenseViewModel
import com.thanesgroup.lgs.ui.component.BarcodeScanner
import com.thanesgroup.lgs.ui.theme.LgsBlue

@Composable
fun DispenseScreen(
  dispenseViewModel: DispenseViewModel,
  contentPadding: PaddingValues,
  context: Context
) {
  LaunchedEffect(dispenseViewModel.errorMessage) {
    if (dispenseViewModel.errorMessage.isNotEmpty()) {
      Toast.makeText(context, dispenseViewModel.errorMessage, Toast.LENGTH_SHORT).show()
      dispenseViewModel.errorMessage = ""
    }
  }

  BarcodeScanner { scannedCode ->
    if (dispenseViewModel.dispenseData == null) {
      dispenseViewModel.handleDispense(scannedCode)
    } else {
      val hn = dispenseViewModel.dispenseData!!.hn
      if (hn != scannedCode) {
        Log.d("HN", hn)
      }
      // สแกนเปิดไฟจัดยา
//       handleReceive(scannedCode)
    }
  }

  if (dispenseViewModel.isLoading) {
    Box(
      modifier = Modifier
        .fillMaxSize(),
      contentAlignment = Alignment.Center
    ) {
      CircularProgressIndicator(
        modifier = Modifier.size(24.dp),
        color = LgsBlue,
        strokeWidth = 2.dp
      )
    }
  } else if (dispenseViewModel.dispenseData == null) {
    ScanPromptUI(contentPadding)
  } else {
    Box(
      modifier = Modifier.padding(contentPadding)
    ) {
      DispenseListScreen(
        data = dispenseViewModel.dispenseData!!,
        onClear = {
          dispenseViewModel.dispenseData = null
          dispenseViewModel.clearDispenseData()
        }
      )
    }
  }
}

@Composable
private fun ScanPromptUI(contentPadding: PaddingValues) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(contentPadding),
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Text(
        text = "Light Guiding Station",
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.titleLarge
      )
      Text(
        text = "( LGS )",
        fontWeight = FontWeight.Normal,
        style = MaterialTheme.typography.titleLarge
      )
      Spacer(modifier = Modifier.height(22.dp))
      Text(
        text = "Scan Barcode / QRCode",
        style = MaterialTheme.typography.bodyLarge
      )
      Spacer(modifier = Modifier.height(42.dp))

      Box(
        modifier = Modifier
          .size(200.dp)
          .clip(CircleShape)
          .padding(4.dp)
          .border(BorderStroke(4.dp, LgsBlue), CircleShape), contentAlignment = Alignment.Center
      ) {
        Icon(
          painter = painterResource(id = R.drawable.lgs_scan),
          contentDescription = "Scan Barcode or QRCode",
          modifier = Modifier.size(100.dp),
          tint = MaterialTheme.colorScheme.onSurface
        )
      }
      Spacer(modifier = Modifier.height(42.dp))

      Text(
        text = "สแกน hn",
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.titleLarge
      )
      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = "จัดยาตามรายการยาในใบสั่งยาที่ทำการระบุ",
        style = MaterialTheme.typography.bodyMedium
      )
    }
  }
}

@Composable
private fun DispenseListScreen(data: DispenseModel, onClear: () -> Unit) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
  ) {
    Text(text = "HN: ${data.hn}", style = MaterialTheme.typography.titleSmall)
    Text(
      text = data.patientName,
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(16.dp))

    LazyColumn(
      modifier = Modifier
        .weight(1f),
    ) {
      items(data.orders) { order ->
        OrderItemCard(order = order)
      }
    }
    Spacer(modifier = Modifier.height(12.dp))
    Button(
      onClick = onClear,
      modifier = Modifier
        .fillMaxWidth()
        .height(42.dp),
      colors = ButtonDefaults.buttonColors(LgsBlue)
    ) {
      Text("สแกนรายการใหม่", fontWeight = FontWeight.Bold, color = Color.White)
    }
    Spacer(modifier = Modifier.height(12.dp))
  }
}

@Composable
private fun OrderItemCard(order: OrderModel) {
  val statusText: String
  val statusColor: Color

  when (order.f_dispensestatus) {
    "0" -> {
      statusText = "ยังไม่มีการจ่ายยา"
      statusColor = Color(0xFFFFA726)
    }

    "1" -> {
      statusText = "จ่ายยาแล้ว"
      statusColor = Color(0xFF66BB6A)
    }

    else -> {
      statusText = "ไม่ทราบสถานะ"
      statusColor = MaterialTheme.colorScheme.onSurfaceVariant
    }
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(bottom = 16.dp)
      .border(
        width = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant,
        shape = RoundedCornerShape(12.dp)
      ),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text(
        text = order.f_orderitemname,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Bold,
        color = LgsBlue
      )
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = "จำนวน: ${order.f_orderqty} ${order.f_orderunitdesc}",
          style = MaterialTheme.typography.bodyMedium
        )
        Text(
          text = "ตำแหน่ง: ${order.f_itemlocationno}",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(statusColor)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = statusText,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Bold,
          color = statusColor
        )
      }
    }
  }
}