package com.thanesgroup.lgs.screen.appUpdate

import android.content.Intent
import android.net.Uri
import android.provider.Settings
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.PullRefreshState
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.ProgressIndicatorDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.navigation.NavHostController
import com.thanesgroup.lgs.R
import com.thanesgroup.lgs.data.model.UpdateInfo
import com.thanesgroup.lgs.data.viewModel.UpdateState
import com.thanesgroup.lgs.data.viewModel.UpdateViewModel
import com.thanesgroup.lgs.ui.theme.Blue80
import com.thanesgroup.lgs.ui.theme.LgsBlue

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun AppUpdateScreen(
  updateViewModel: UpdateViewModel, navController: NavHostController
) {
  val context = LocalContext.current
  val updateState by updateViewModel.updateState.collectAsState()
  val updateInfo by updateViewModel.updateInfo.collectAsState()
  var isLoading by remember { mutableStateOf(false) }

  val installPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
  ) {
    updateViewModel.checkForUpdate()
  }

  val pullRefreshState = rememberPullRefreshState(
    refreshing = isLoading,
    onRefresh = {
      isLoading = true
      updateViewModel.checkForUpdate()
    }
  )

  LaunchedEffect(updateState) {
    when (updateState) {
      is UpdateState.Checking -> {
        isLoading = true
      }

      is UpdateState.Idle,
      is UpdateState.Failed,
      is UpdateState.UpdateAvailable -> {
        isLoading = false
      }

      else -> {}
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(title = {
        Text(
          text = "อัปเดตแอพ",
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
              .background(MaterialTheme.colorScheme.background)
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
    Column(
      modifier = Modifier
        .padding(innerPadding)
        .fillMaxSize()
        .padding(horizontal = 16.dp)
    ) {
      when (updateState) {
        is UpdateState.Checking -> {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
        }

        is UpdateState.Failed -> {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
              modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center
            ) {
              Text(
                "เกิดข้อผิดพลาดในการตรวจสอบอัปเดต",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelLarge,
              )
              Spacer(modifier = Modifier.height(24.dp))
              Button(
                onClick = { updateViewModel.checkForUpdate() },
                colors = ButtonDefaults.buttonColors(containerColor = LgsBlue)
              ) {
                Text("ลองอีกครั้ง", color = Color.White)
              }
            }
          }
        }

        is UpdateState.Idle -> {
          LatestVersionUI(updateViewModel, pullRefreshState, isLoading)
        }

        else -> {
          SoftwareUpdateInfo(
            updateState = updateState,
            updateInfo = updateInfo,
            onDownloadClick = { info ->
              if (context.packageManager.canRequestPackageInstalls()) {
                updateViewModel.downloadUpdate(info)
              } else {
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                  data = "package:${context.packageName}".toUri()
                }
                installPermissionLauncher.launch(intent)
              }
            },
            onInstallClick = { uri ->
              updateViewModel.installUpdate(context, uri)
            })
        }
      }
    }
  }
}

@Composable
private fun SoftwareUpdateInfo(
  updateState: UpdateState,
  updateInfo: UpdateInfo?,
  onDownloadClick: (UpdateInfo) -> Unit,
  onInstallClick: (Uri) -> Unit
) {
  Column {
    if (updateInfo != null) {
      Text(
        "เวอร์ชันใหม่พร้อมให้ติดตั้ง",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )
      Spacer(Modifier.height(8.dp))
      Text("LGS ${updateInfo.version_name}", style = MaterialTheme.typography.titleMedium)
      HorizontalDivider(
        modifier = Modifier.padding(vertical = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant
      )

      Text(
        "มีอะไรใหม่", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold
      )
      Spacer(Modifier.height(8.dp))

      Column(
        modifier = Modifier
          .heightIn(max = 350.dp)
          .verticalScroll(rememberScrollState())
      ) {
        val rawChangelog = updateInfo.changelog
        val formattedChangelog = rawChangelog.replace("\\n", "\n")

        Text(
          text = formattedChangelog,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 18.dp),
        contentAlignment = Alignment.Center
      ) {
        when (updateState) {
          is UpdateState.UpdateAvailable -> {
            Button(
              onClick = { onDownloadClick(updateInfo) },
              modifier = Modifier.fillMaxWidth(),
              colors = ButtonDefaults.buttonColors(LgsBlue)
            ) {
              Text("ดาวน์โหลดและติดตั้ง", color = Color.White)
            }
          }

          is UpdateState.checkFile -> {
            Text("กำลังเตรียมพร้อมสำหรับการติดตั้ง", style = MaterialTheme.typography.labelLarge)
          }

          is UpdateState.Downloading -> {
            DownloadProgress(updateState.progress)
          }

          is UpdateState.DownloadComplete -> {
            Button(
              onClick = { onInstallClick(updateState.fileUri) },
              modifier = Modifier.fillMaxWidth(),
              colors = ButtonDefaults.buttonColors(containerColor = LgsBlue)
            ) {
              Text("ติดตั้งตอนนี้", color = Color.White)
            }
          }

          else -> {
          }
        }
      }
      HorizontalDivider(
        modifier = Modifier.padding(vertical = 12.dp),
        color = MaterialTheme.colorScheme.outlineVariant
      )
    }
  }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun LatestVersionUI(
  updateViewModel: UpdateViewModel,
  pullRefreshState: PullRefreshState,
  isLoading: Boolean
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .pullRefresh(pullRefreshState)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(top = 48.dp)
        .verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Icon(
        painter = painterResource(id = R.drawable.check_circle_24px),
        contentDescription = "Latest Version",
        modifier = Modifier.size(52.dp)
      )
      Spacer(modifier = Modifier.height(12.dp))
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
        text = "แอพของคุณเป็นเวอร์ชันล่าสุด",
        style = MaterialTheme.typography.labelLarge,
        color = Color.Gray
      )
    }

    PullRefreshIndicator(
      refreshing = isLoading,
      state = pullRefreshState,
      backgroundColor = MaterialTheme.colorScheme.background,
      contentColor = LgsBlue,
      modifier = Modifier
        .align(Alignment.TopCenter)
        .padding(top = 24.dp)
    )
  }
}

@Composable
private fun DownloadProgress(progress: Int) {
  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.Start
  ) {
    LinearProgressIndicator(
      progress = { progress / 101f },
      modifier = Modifier.fillMaxWidth(),
      trackColor = Blue80,
      color = LgsBlue,
      strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
      gapSize = 6.dp
    )
    Spacer(modifier = Modifier.height(10.dp))
    Text("กำลังดาวน์โหลด... $progress%", style = MaterialTheme.typography.labelLarge)
  }
}
