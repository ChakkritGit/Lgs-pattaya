package com.thanesgroup.lgs.screen.dispense

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thanesgroup.lgs.R
import com.thanesgroup.lgs.data.model.DispenseModel
import com.thanesgroup.lgs.data.model.OrderModel
import com.thanesgroup.lgs.data.repositories.ApiRepository
import com.thanesgroup.lgs.data.repository.SettingsRepository
import com.thanesgroup.lgs.data.viewModel.DataStoreViewModel
import com.thanesgroup.lgs.data.viewModel.DataStoreViewModelFactory
import com.thanesgroup.lgs.ui.component.BarcodeScanner
import com.thanesgroup.lgs.ui.theme.LgsBlue
import com.thanesgroup.lgs.util.parseErrorMessage
import com.thanesgroup.lgs.util.parseExceptionMessage
import kotlinx.coroutines.launch

@Composable
fun DispenseScreen(
  context: Context
) {
  val scope = rememberCoroutineScope()
  var errorMessage by remember { mutableStateOf("") }

  var dispenseData by remember { mutableStateOf<DispenseModel?>(null) }
  var isLoading by remember { mutableStateOf(false) }

  val dataStoreViewModel: DataStoreViewModel = viewModel(
    factory = DataStoreViewModelFactory(SettingsRepository.getInstance(context))
  )

  val storedHn by dataStoreViewModel.hn.collectAsState()

  fun handleDispense(hn: String) {
    if (hn.isEmpty()) return
    isLoading = true
    scope.launch {
      try {
        val response = ApiRepository.dispense(hn)

        if (response.isSuccessful) {
          val data = response.body()?.data
          if (data != null) {
            dispenseData = data
            dataStoreViewModel.saveHn(hn)
          } else {
            errorMessage = "ไม่พบข้อมูลใบสั่งยา"
            dispenseData = null
            dataStoreViewModel.clearHn()
          }
        } else {
          val errorJson = response.errorBody()?.string()
          errorMessage = parseErrorMessage(response.code(), errorJson)
          dispenseData = null
          dataStoreViewModel.clearHn()
        }
      } catch (e: Exception) {
        errorMessage = parseExceptionMessage(e)
        dispenseData = null
        dataStoreViewModel.clearHn()
      } finally {
        isLoading = false
      }
    }
  }

  fun handleReceive(orderCode: String) {
    // เปิดไฟจัดยา
  }

  LaunchedEffect(errorMessage) {
    if (errorMessage.isNotEmpty()) {
      Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
      errorMessage = ""
    }
  }

  LaunchedEffect(storedHn) {
    if (dispenseData == null && storedHn.isNotEmpty() && storedHn != "Loading...") {
      handleDispense(storedHn)
    }
  }

  BarcodeScanner { scannedCode ->
    if (dispenseData == null) {
      handleDispense(scannedCode)
    } else {
      // สแกนเปิดไฟจัดยา
//       handleReceive(scannedCode)
    }
  }

  if (isLoading) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      CircularProgressIndicator(
        modifier = Modifier.size(32.dp),
        color = LgsBlue,
        strokeWidth = 2.dp
      )
    }
  } else if (dispenseData == null) {
    ScanPromptUI()
  } else {
    DispenseListScreen(
      data = dispenseData!!,
      onClear = {
        dispenseData = null
        dataStoreViewModel.clearHn()
      }
    )
  }
}

@Composable
private fun ScanPromptUI() {
  Box(
    modifier = Modifier
      .fillMaxSize(),
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
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
      )
      Text(
        text = "( LGS )", fontSize = 28.sp, color = MaterialTheme.colorScheme.onBackground
      )
      Spacer(modifier = Modifier.height(22.dp))
      Text(
        text = "Scan Barcode / QRCode",
        fontSize = 20.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(42.dp))

      Box(
        modifier = Modifier
          .size(220.dp)
          .clip(CircleShape)
          .padding(4.dp)
          .border(BorderStroke(4.dp, LgsBlue), CircleShape), contentAlignment = Alignment.Center
      ) {
        Icon(
          painter = painterResource(id = R.drawable.lgs_scan),
          contentDescription = "Scan Barcode or QRCode",
          modifier = Modifier.size(120.dp),
          tint = MaterialTheme.colorScheme.onSurface
        )
      }
      Spacer(modifier = Modifier.height(42.dp))

      Text(
        text = "สแกน hn",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
      )
      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = "จัดยาตามรายการยาในใบสั่งยาที่ทำการระบุ",
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
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
    Text(text = "HN: ${data.hn}", style = MaterialTheme.typography.titleMedium)
    Text(
      text = data.patientName,
      style = MaterialTheme.typography.titleLarge,
      fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(16.dp))

    LazyColumn(
      modifier = Modifier
        .weight(1f),
//      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      items(data.orders) { order ->
        OrderItemCard(order = order)
      }
    }
    Spacer(modifier = Modifier.height(12.dp))
    Button(
      onClick = onClear,
      modifier = Modifier.fillMaxWidth(),
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
//  val cardBackgroundColor: Color

  when (order.f_dispensestatus) {
    "0" -> {
      statusText = "รอจัดยา"
      statusColor = Color(0xFFFFA726)
//      cardBackgroundColor = MaterialTheme.colorScheme.surface
    }

    "1" -> {
      statusText = "จัดยาแล้ว"
      statusColor = Color(0xFF66BB6A)
//      cardBackgroundColor = MaterialTheme.colorScheme.surfaceContainerLowest
    }

    else -> {
      statusText = "ไม่ทราบสถานะ"
      statusColor = MaterialTheme.colorScheme.onSurfaceVariant
//      cardBackgroundColor = MaterialTheme.colorScheme.surface
    }
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(bottom = 16.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.3.dp),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
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