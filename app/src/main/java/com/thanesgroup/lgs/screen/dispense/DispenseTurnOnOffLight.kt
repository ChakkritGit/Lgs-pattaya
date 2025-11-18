package com.thanesgroup.lgs.screen.dispense

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.thanesgroup.lgs.R
import com.thanesgroup.lgs.data.viewModel.DispenseViewModel
import com.thanesgroup.lgs.ui.component.BarcodeScanner
import com.thanesgroup.lgs.ui.theme.LgsBlue
import com.thanesgroup.lgs.ui.theme.anuphanFamily
import kotlinx.coroutines.launch

@Composable
fun DispenseTurnOnOffLight(
  contentPadding: PaddingValues, dispenseViewModel: DispenseViewModel, context: Context
) {
  val scope = rememberCoroutineScope()
  var retryBarcode by remember { mutableStateOf("") }
  var showRetryGetLabelDialog by remember { mutableStateOf(false) }
  var showRetryReceiveDialog by remember { mutableStateOf(false) }
  var isRetryGetLabelLoading by remember { mutableStateOf(false) }

  BarcodeScanner { scannedCode ->
    if (dispenseViewModel.dispenseOnData == null) {
      scope.launch {
        val result = dispenseViewModel.handleDispenseOnManual(scannedCode = scannedCode)

        if (result == null) {
          retryBarcode = scannedCode
          showRetryGetLabelDialog = true

          return@launch
        }

        retryBarcode = ""
      }
    } else {
      if (dispenseViewModel.dispenseOnData?.location == scannedCode) {
        scope.launch {
          val result =
            dispenseViewModel.handleDispenseOffManual(scannedCode = dispenseViewModel.dispenseOnData?.location!!)

          if (result == null) {
            retryBarcode = scannedCode
            showRetryReceiveDialog = true

            return@launch
          }

          retryBarcode = ""
        }
      }
    }
  }

  LaunchedEffect(dispenseViewModel.errorMessage) {
    if (dispenseViewModel.errorMessage.isNotEmpty()) {
      Toast.makeText(context, dispenseViewModel.errorMessage, Toast.LENGTH_SHORT).show()
      dispenseViewModel.errorMessage = ""
    }
  }

  Scaffold(
    modifier = Modifier.padding(contentPadding)
  ) { _ ->
    Box(
      modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
      if (dispenseViewModel.isLoading) {
        CircularProgressIndicator(
          modifier = Modifier.size(24.dp), color = LgsBlue, strokeWidth = 2.dp
        )
      } else {
        if (dispenseViewModel.dispenseOnData != null) {
          Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Row {
              Text(
                text = "BinLo : ",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = dispenseViewModel.dispenseOnData?.location ?: "",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.Red
              )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
              text = dispenseViewModel.dispenseOnData?.drugName ?: "ไม่พบชื่อยา",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis
            )
          }
        } else {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .verticalScroll(rememberScrollState())
              .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
          ) {
            androidx.compose.material3.Text(
              text = "Light Guiding Station",
              fontWeight = FontWeight.Bold,
              style = MaterialTheme.typography.titleLarge
            )
            androidx.compose.material3.Text(
              text = "( LGS )",
              fontWeight = FontWeight.Normal,
              style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(22.dp))
            androidx.compose.material3.Text(
              text = "Scan Barcode / QRCode", style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(42.dp))

            Box(
              modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .padding(4.dp)
                .border(BorderStroke(4.dp, LgsBlue), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              androidx.compose.material3.Icon(
                painter = painterResource(id = R.drawable.lgs_scan),
                contentDescription = "Scan Barcode or QRCode",
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.onSurface
              )
            }
            Spacer(modifier = Modifier.height(42.dp))

            androidx.compose.material3.Text(
              text = "สแกน Drug Code",
              fontWeight = FontWeight.Bold,
              style = MaterialTheme.typography.titleLarge
            )
          }
        }
      }
    }
  }

  if (showRetryGetLabelDialog) {
    Dialog(
      onDismissRequest = { }, properties = DialogProperties(
        dismissOnBackPress = false, dismissOnClickOutside = false, usePlatformDefaultWidth = false
      )
    ) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 32.dp)
          .border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline,
            shape = RoundedCornerShape(34.dp)
          ),
        shape = RoundedCornerShape(34.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
      ) {
        Column(
          modifier = Modifier.padding(top = 14.dp, start = 14.dp, end = 14.dp, bottom = 10.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "จัดยาล้มเหลว",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge
          )
          Spacer(modifier = Modifier.height(4.dp))

          Text(
            "กรุณาลองใหม่อีกครั้ง", style = MaterialTheme.typography.bodyMedium
          )

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = {
                showRetryGetLabelDialog = false
                retryBarcode = ""
              },
              shape = CircleShape,
              modifier = Modifier.weight(1f),
              colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
              )
            ) {
              Text(
                text = "ปิด",
                fontFamily = anuphanFamily,
                style = MaterialTheme.typography.labelLarge
              )
            }

            Button(
              onClick = {
                scope.launch {
                  isRetryGetLabelLoading = true

                  val labelData = dispenseViewModel.handleDispenseOnManual(retryBarcode)

                  if (labelData == null) {
                    dispenseViewModel.errorMessage = "จัดยาล้มเหลว"

                    isRetryGetLabelLoading = false
                    return@launch
                  }

                  retryBarcode = ""
                  showRetryGetLabelDialog = false
                  isRetryGetLabelLoading = false
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = LgsBlue),
              shape = CircleShape,
              modifier = Modifier.weight(1f),
              enabled = !isRetryGetLabelLoading
            ) {
              if (!isRetryGetLabelLoading) {
                Text(
                  text = "ลองอีกครั้ง",
                  fontFamily = anuphanFamily,
                  style = MaterialTheme.typography.labelLarge,
                  color = Color.White
                )
              } else {
                CircularProgressIndicator(
                  modifier = Modifier.size(24.dp), color = LgsBlue, strokeWidth = 2.dp
                )
              }
            }
          }
        }
      }
    }
  }

  if (showRetryReceiveDialog) {
    Dialog(
      onDismissRequest = { }, properties = DialogProperties(
        dismissOnBackPress = false, dismissOnClickOutside = false, usePlatformDefaultWidth = false
      )
    ) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 32.dp)
          .border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline,
            shape = RoundedCornerShape(34.dp)
          ),
        shape = RoundedCornerShape(34.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
      ) {
        Column(
          modifier = Modifier.padding(top = 14.dp, start = 14.dp, end = 14.dp, bottom = 10.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "จัดยาล้มเหลว",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge
          )
          Spacer(modifier = Modifier.height(4.dp))

          Text(
            "กรุณาลองใหม่อีกครั้ง", style = MaterialTheme.typography.bodyMedium
          )

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = {
                showRetryReceiveDialog = false
                retryBarcode = ""
              },
              shape = CircleShape,
              modifier = Modifier.weight(1f),
              colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
              )
            ) {
              Text(
                text = "ปิด",
                fontFamily = anuphanFamily,
                style = MaterialTheme.typography.labelLarge
              )
            }

            Button(
              onClick = {
                scope.launch {
                  isRetryGetLabelLoading = true

                  val labelData = dispenseViewModel.handleDispenseOffManual(retryBarcode)

                  if (labelData == null) {
                    dispenseViewModel.errorMessage = "รับยาล้มเหลว"

                    isRetryGetLabelLoading = false
                    return@launch
                  }

                  retryBarcode = ""
                  showRetryReceiveDialog = false
                  isRetryGetLabelLoading = false
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = LgsBlue),
              shape = CircleShape,
              modifier = Modifier.weight(1f),
              enabled = !isRetryGetLabelLoading
            ) {
              if (!isRetryGetLabelLoading) {
                Text(
                  text = "ลองอีกครั้ง",
                  fontFamily = anuphanFamily,
                  style = MaterialTheme.typography.labelLarge,
                  color = Color.White
                )
              } else {
                CircularProgressIndicator(
                  modifier = Modifier.size(24.dp), color = LgsBlue, strokeWidth = 2.dp
                )
              }
            }
          }
        }
      }
    }
  }
}
