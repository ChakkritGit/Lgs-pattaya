package com.thanesgroup.lgs.screen.appUpdate

import android.content.Intent
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.thanesgroup.lgs.R
import com.thanesgroup.lgs.data.model.UpdateInfo
import com.thanesgroup.lgs.data.viewModel.UpdateState
import com.thanesgroup.lgs.data.viewModel.UpdateViewModel
import com.thanesgroup.lgs.ui.theme.LgsBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppUpdateScreen(
  updateViewModel: UpdateViewModel,
  navController: NavHostController
) {
  val context = LocalContext.current
  val updateState by updateViewModel.updateState.collectAsState()

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
      TopAppBar(title = {
        Text(
          text = "การอัปเดตซอฟต์แวร์",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
      }, navigationIcon = {
        TooltipBox(
          positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
          tooltip = { PlainTooltip { Text("ย้อนกลับ") } },
          state = remember { TooltipState() }) {
          Box(
            modifier = Modifier
              .padding(12.dp)
              .shadow(elevation = 0.7.dp, shape = CircleShape)
              .size(36.dp)
              .clip(CircleShape)
              .border(
                width = 1.dp, color = MaterialTheme.colorScheme.outline, shape = CircleShape
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
      })
    }) { innerPadding ->
    Box(
      modifier = Modifier
        .padding(innerPadding)
        .fillMaxSize()
        .padding(horizontal = 16.dp)
    ) {
      when (val state = updateState) {
        is UpdateState.Checking -> {
          Column(
            modifier = Modifier
              .fillMaxSize()
              .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            CircularProgressIndicator(
              modifier = Modifier.size(24.dp), color = LgsBlue, strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              "กำลังตรวจสอบอัปเดต...",
              style = MaterialTheme.typography.labelLarge,
            )
          }
        }

        is UpdateState.Idle -> {
          Column(
            modifier = Modifier
              .fillMaxSize()
              .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            LatestVersionUI(updateViewModel = updateViewModel)
          }
        }

        is UpdateState.UpdateAvailable -> {
          UpdateDetails(
            updateInfo = state.info, onDownloadClick = {
              if (context.packageManager.canRequestPackageInstalls()) {
                updateViewModel.downloadUpdate(state.info)
              } else {
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                  data = "package:${context.packageName}".toUri()
                }
                installPermissionLauncher.launch(intent)
              }
            })
        }

        is UpdateState.Downloading -> {
          Column(
            modifier = Modifier
              .fillMaxSize()
              .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            DownloadProgress(progress = state.progress)
          }
        }

        is UpdateState.DownloadComplete -> {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.Center
          ) {
            Button(
              onClick = { updateViewModel.installUpdate(context, state.fileUri) },
              modifier = Modifier.fillMaxWidth(),
              colors = ButtonDefaults.buttonColors(LgsBlue)
            ) {
              Text("ติดตั้งตอนนี้", color = Color.White)
            }
          }
        }

        is UpdateState.Failed -> {
          Column(
            modifier = Modifier
              .fillMaxSize()
              .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Text("เกิดข้อผิดพลาด: ${state.message}", color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
              onClick = { updateViewModel.checkForUpdate() },
              colors = ButtonDefaults.buttonColors(LgsBlue)
            ) {
              Text("ลองอีกครั้ง", color = Color.White)
            }
          }
        }
      }
    }
  }

  if (updateState is UpdateState.Failed) {
    val message = (updateState as UpdateState.Failed).message
    LaunchedEffect(message) {
      Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
  }
}

@Composable
private fun LatestVersionUI(updateViewModel: UpdateViewModel) {
  Column(
    verticalArrangement = Arrangement.spacedBy(2.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Icon(
      painter = painterResource(id = R.drawable.check_circle_24px),
      contentDescription = "Latest Version",
      modifier = Modifier.size(64.dp)
    )
    Spacer(modifier = Modifier.height(16.dp))
    Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "LGS", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold
      )
      Text(
        text = updateViewModel.getBuildVersion(),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
    }
    Text(
      text = "ซอฟต์แวร์ของคุณเป็นเวอร์ชันล่าสุด",
      style = MaterialTheme.typography.labelLarge,
      color = Color.Gray
    )
  }
}

@Composable
private fun UpdateDetails(updateInfo: UpdateInfo, onDownloadClick: () -> Unit) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 12.dp), horizontalAlignment = Alignment.Start
  ) {
    Text("LGS ${updateInfo.versionName}", style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(16.dp))
    Text("มีอะไรใหม่", style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      text = updateInfo.changelog,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(24.dp))
    Button(
      onClick = onDownloadClick,
      modifier = Modifier.fillMaxWidth(),
      colors = ButtonDefaults.buttonColors(LgsBlue)
    ) {
      Text("ดาวน์โหลดและติดตั้ง", color = Color.White)
    }
  }
}

@Composable
private fun DownloadProgress(progress: Int) {
  Column(
    modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
  ) {
    LinearProgressIndicator(
      progress = { progress / 100f }, modifier = Modifier.fillMaxWidth(), color = LgsBlue
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text("กำลังดาวน์โหลด... $progress%")
  }
}
