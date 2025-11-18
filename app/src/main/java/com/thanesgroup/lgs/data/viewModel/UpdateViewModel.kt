package com.thanesgroup.lgs.data.viewModel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thanesgroup.lgs.BuildConfig
import com.thanesgroup.lgs.data.model.UpdateInfo
import com.thanesgroup.lgs.data.repositories.ApiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

sealed class UpdateState {
  object Idle : UpdateState()
  object Checking : UpdateState()
  data class UpdateAvailable(val info: UpdateInfo) : UpdateState()
  data class Downloading(val progress: Int) : UpdateState()
  data class DownloadComplete(val fileUri: Uri) : UpdateState()
  data class Failed(val message: String) : UpdateState()
  data class checkFile(val message: String) : UpdateState()
}

class UpdateViewModel(application: Application) : AndroidViewModel(application) {

  private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
  val updateState = _updateState.asStateFlow()
  private var downloadedFileUri: Uri? = null
  private val _updateInfo = MutableStateFlow<UpdateInfo?>(null)
  val updateInfo = _updateInfo.asStateFlow()
  private val buildVersionName = BuildConfig.VERSION_NAME

  private fun calculateSha256(file: File): String {
    val digest = MessageDigest.getInstance("SHA-256")
    file.inputStream().use { fis ->
      val buffer = ByteArray(8192)
      var bytesRead: Int
      while (fis.read(buffer).also { bytesRead = it } != -1) {
        digest.update(buffer, 0, bytesRead)
      }
    }

    return digest.digest().joinToString("") { "%02x".format(it) }
  }

  private fun verifyPackageName(apkFile: File): Boolean {
    try {
      val packageManager = getApplication<Application>().packageManager
      val packageInfo = packageManager.getPackageArchiveInfo(apkFile.absolutePath, 0)

      if (packageInfo != null) {
        return packageInfo.packageName == BuildConfig.APPLICATION_ID
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
    return false
  }

  fun checkForUpdate() {
    viewModelScope.launch {
      _updateState.value = UpdateState.Checking
      _updateInfo.value = null

      try {
        val response = ApiRepository.getUpdate()
        if (response.isSuccessful && response.body() != null) {
          val updateInfo = response.body()!!.data
          val currentVersionCode = BuildConfig.VERSION_CODE
          if (updateInfo.version_code.toInt() > currentVersionCode) {
            _updateInfo.value = updateInfo
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
    viewModelScope.launch(Dispatchers.IO) {
      try {
        val response = ApiRepository.downloadUpdateFile(updateInfo.apk_url)

        if (response.isSuccessful) {
          val body = response.body() ?: throw Exception("Response body empty")
          val file = File(getApplication<Application>().externalCacheDir, "update.apk")

          body.byteStream().use { input ->
            FileOutputStream(file).use { output ->
              val buffer = ByteArray(8 * 1024)
              var bytesRead: Int
              var totalRead = 0L
              val contentLength = body.contentLength()

              while (input.read(buffer).also { bytesRead = it } != -1) {
                output.write(buffer, 0, bytesRead)
                totalRead += bytesRead
                val progress = if (contentLength > 0)
                  ((totalRead * 100) / contentLength).toInt()
                else -1
                _updateState.value = UpdateState.Downloading(progress)
              }
            }
          }

          _updateState.value = UpdateState.checkFile("check sha256 chunk sum")

          val downloadedFileSha256 = calculateSha256(file)
          if (downloadedFileSha256.equals(updateInfo.checksum, ignoreCase = true)) {
            Log.d("UpdateViewModel", "SHA-256 checksum verification successful.")
          } else {
            Log.d(
              "UpdateViewModel",
              "SHA-256 checksum verification FAILED. Expected: ${updateInfo.checksum}, Got: $downloadedFileSha256"
            )
            file.delete()
            throw Exception("ไฟล์อัปเดตไม่ถูกต้อง (Checksum mismatch)")
          }

          if (verifyPackageName(file)) {
            Log.d("UpdateViewModel", "Package name verification successful.")
          } else {
            Log.d(
              "UpdateViewModel",
              "Package name verification FAILED. This might be a malicious APK."
            )
            file.delete()
            throw Exception("ไฟล์อัปเดตไม่ถูกต้อง (Package name mismatch)")
          }

          val fileUri = FileProvider.getUriForFile(
            getApplication(), "${BuildConfig.APPLICATION_ID}.fileprovider", file
          )
          downloadedFileUri = fileUri
          _updateState.value = UpdateState.DownloadComplete(fileUri)

        } else {
          _updateState.value = UpdateState.Failed("ดาวน์โหลดล้มเหลว")
        }
      } catch (e: Exception) {
        _updateState.value = UpdateState.Failed("เกิดข้อผิดพลาด: ${e.message}")
      }
    }
  }

  fun installUpdate(context: Context, fileUri: Uri) {
    val file = File(getApplication<Application>().externalCacheDir, "update.apk")
    val contentUri = FileProvider.getUriForFile(
      context, "${context.packageName}.fileprovider",
      file
    )
    val intent = Intent(Intent.ACTION_VIEW).apply {
      setDataAndType(contentUri, "application/vnd.android.package-archive")
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(intent)
  }

  fun getBuildVersion(): String {
    return buildVersionName
  }
}
