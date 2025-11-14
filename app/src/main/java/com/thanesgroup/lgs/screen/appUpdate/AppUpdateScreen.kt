package com.thanesgroup.lgs.screen.appUpdate

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TooltipState
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.thanesgroup.lgs.R
import com.thanesgroup.lgs.data.model.UpdateInfo
import com.thanesgroup.lgs.data.viewModel.UpdateState
import com.thanesgroup.lgs.data.viewModel.UpdateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppUpdateScreen(
  navController: NavHostController
) {
  val context = LocalContext.current
  val updateViewModel: UpdateViewModel = viewModel()
  val updateState by updateViewModel.updateState.collectAsState()
  val tooltipState = remember { TooltipState() }
  val positionProvider = TooltipDefaults.rememberTooltipPositionProvider()

  val installPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
  ) {
    updateViewModel.checkForUpdate()
  }

  LaunchedEffect(Unit) {
    updateViewModel.checkForUpdate()
  }

  Scaffold(
    topBar = {
      TopAppBar(
        navigationIcon = {
          TooltipBox(
            positionProvider = positionProvider,
            tooltip = {
              PlainTooltip {
                Text("ย้อนกลับ")
              }
            },
            state = tooltipState
          ) {
            Box(
              modifier = Modifier
                .padding(14.dp)
                .shadow(elevation = 0.7.dp, shape = CircleShape)
                .size(42.dp)
                .clip(CircleShape)
                .border(
                  width = 1.dp,
                  color = MaterialTheme.colorScheme.outlineVariant,
                  shape = CircleShape
                )
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .clickable(onClick = { navController.popBackStack() }),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(R.drawable.arrow_back_ios_new_24px),
                contentDescription = "arrow_back_ios_new_24px",
                tint = MaterialTheme.colorScheme.onSurface
              )
            }
          }
        },
        title = { Text(text = "การอัปเดทซอฟแวร์", fontWeight = FontWeight.Bold) },
      )
    },
    containerColor = Color.Transparent
  ) { _ ->
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Icon(
          painter = painterResource(id = R.drawable.check_circle_24px),
          contentDescription = "check_circle_24px",
          modifier = Modifier.size(42.dp),
          tint = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "เวอร์ชั่น ${updateViewModel.getBuildVersion()}",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "เวอร์ชั่นล่าสุด",
          fontSize = 14.sp
        )
      }
    }

    when (val state = updateState) {
      is UpdateState.UpdateAvailable -> {
        UpdateAvailableDialog(
          updateInfo = state.info,
          onUpdateClick = {
            if (context.packageManager.canRequestPackageInstalls()) {
              updateViewModel.downloadUpdate(state.info)
            } else {
              val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${context.packageName}")
              }
              installPermissionLauncher.launch(intent)
            }
          },
          onDismiss = { /* ทำอะไรบางอย่างเมื่อผู้ใช้ไม่อัปเดต */ }
        )
      }

      is UpdateState.Downloading -> {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("กำลังดาวน์โหลด...")
          }
        }
      }

      is UpdateState.DownloadComplete -> {
        LaunchedEffect(Unit) {
          updateViewModel.installUpdate(context, state.fileUri)
        }
      }

      is UpdateState.Failed -> {
        LaunchedEffect(state.message) {
          Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
        }
      }

      else -> {
        // ไม่ต้องทำอะไรในสถานะ Idle หรือ Checking
      }
    }
  }
}

@Composable
fun UpdateAvailableDialog(
  updateInfo: UpdateInfo,
  onUpdateClick: () -> Unit,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("มีอัปเดตใหม่!") },
    text = {
      Column {
        Text("เวอร์ชันใหม่ ${updateInfo.versionName} พร้อมให้ติดตั้งแล้ว")
        Spacer(modifier = Modifier.height(8.dp))
        Text("มีอะไรใหม่:")
        Text(updateInfo.changelog)
      }
    },
    confirmButton = {
      Button(onClick = onUpdateClick) {
        Text("อัปเดตทันที")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("ภายหลัง")
      }
    }
  )
}