package com.thanesgroup.lgs.data.viewModel

import android.app.Application
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thanesgroup.lgs.BuildConfig
import com.thanesgroup.lgs.data.model.UpdateInfo
import com.thanesgroup.lgs.data.repositories.ApiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class UpdateState {
  object Idle : UpdateState()
  object Checking : UpdateState()
  data class UpdateAvailable(val info: UpdateInfo) : UpdateState()
  object Downloading : UpdateState()
  data class DownloadComplete(val fileUri: Uri) : UpdateState()
  data class Failed(val message: String) : UpdateState()
}

class UpdateViewModel(application: Application) : AndroidViewModel(application) {

  private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
  private val buildVersionName = BuildConfig.VERSION_NAME
  val updateState = _updateState.asStateFlow()

  private var downloadId: Long = -1L
  private val downloadManager =
    application.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

  private val onDownloadComplete = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
      if (id == downloadId) {
        val uri = downloadManager.getUriForDownloadedFile(downloadId)
        if (uri != null) {
          _updateState.value = UpdateState.DownloadComplete(uri)
        } else {
          _updateState.value = UpdateState.Failed("ไม่สามารถหาไฟล์ที่ดาวน์โหลดเจอ")
        }
        context.unregisterReceiver(this)
      }
    }
  }

  fun checkForUpdate() {
    viewModelScope.launch {
      _updateState.value = UpdateState.Checking
      try {
        val response = ApiRepository.getUpdate()
        if (response.isSuccessful && response.body() != null) {
          val updateInfo = response.body()!!.data
          val currentVersionCode = BuildConfig.VERSION_CODE
          if (updateInfo.versionCode > currentVersionCode) {
            _updateState.value = UpdateState.UpdateAvailable(updateInfo)
          } else {
            _updateState.value = UpdateState.Idle
          }
        } else {
          _updateState.value = UpdateState.Failed("ไม่สามารถตรวจสอบอัปเดตได้")
        }
      } catch (e: Exception) {
        _updateState.value = UpdateState.Failed("เกิดข้อผิดพลาด: ${e.message}")
      }
    }
  }

  fun downloadUpdate(updateInfo: UpdateInfo) {
    val application = getApplication<Application>()
    val request = DownloadManager.Request(Uri.parse(updateInfo.apkUrl))
      .setTitle("กำลังดาวน์โหลดอัปเดต LGS")
      .setDescription("เวอร์ชัน ${updateInfo.versionName}")
      .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
      .setDestinationInExternalFilesDir(application, null, "update.apk")

    downloadId = downloadManager.enqueue(request)
    _updateState.value = UpdateState.Downloading

    application.registerReceiver(
      onDownloadComplete,
      IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
      Context.RECEIVER_NOT_EXPORTED
    )
  }

  fun installUpdate(context: Context, fileUri: Uri) {
    val installIntent = Intent(Intent.ACTION_VIEW).apply {
      setDataAndType(fileUri, "application/vnd.android.package-archive")
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(installIntent)
    _updateState.value = UpdateState.Idle
  }

  fun getBuildVersion(): String {
    return buildVersionName
  }
}