//package com.thanesgroup.lgs.data.viewModel
//
//import android.app.Application
//import android.app.DownloadManager
//import android.content.BroadcastReceiver
//import android.content.Context
//import android.content.Intent
//import android.content.IntentFilter
//import android.net.Uri
//import androidx.lifecycle.AndroidViewModel
//import androidx.lifecycle.viewModelScope
//import com.thanesgroup.lgs.BuildConfig
//import com.thanesgroup.lgs.data.model.UpdateInfo
//import com.thanesgroup.lgs.data.repositories.ApiRepository
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.flow.update
//import kotlinx.coroutines.launch
//import java.io.File
//
//// สถานะต่างๆ ของการอัปเดต
//sealed class UpdateState {
//  object Idle : UpdateState() // สถานะเริ่มต้น
//  object Checking : UpdateState() // กำลังตรวจสอบ
//  data class UpdateAvailable(val info: UpdateInfo) : UpdateState() // มีอัปเดต
//  data class Downloading(val progress: Int) : UpdateState()
//  data class DownloadComplete(val fileUri: Uri) : UpdateState() // ดาวน์โหลดเสร็จ
//  data class Failed(val message: String) : UpdateState() // เกิดข้อผิดพลาด
//}
//
//class UpdateViewModel(application: Application) : AndroidViewModel(application) {
//
//  private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
//  private val buildVersionName = BuildConfig.VERSION_NAME
//  val updateState = _updateState.asStateFlow()
//
//  private var downloadId: Long = -1L
//  private val downloadManager =
//    application.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
//
//  private val onDownloadComplete = object : BroadcastReceiver() {
//    override fun onReceive(context: Context, intent: Intent) {
//      val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
//      if (id == downloadId) {
//        val uri = downloadManager.getUriForDownloadedFile(downloadId)
//        if (uri != null) {
//          _updateState.value = UpdateState.DownloadComplete(uri)
//        } else {
//          _updateState.value = UpdateState.Failed("ไม่สามารถหาไฟล์ที่ดาวน์โหลดเจอ")
//        }
//        context.unregisterReceiver(this)
//      }
//    }
//  }
//
//  fun trackDownloadProgress() {
//    viewModelScope.launch {
//
//      var isDownloading = true
//      var lastProgress = -1
//
//      while (isDownloading) {
//
//        val query = DownloadManager.Query().setFilterById(downloadId)
//        val cursor = downloadManager.query(query)
//
//        if (cursor != null && cursor.moveToFirst()) {
//
//          val status =
//            cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
//
//          val downloaded =
//            cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
//
//          val total =
//            cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
//
//          when (status) {
//
//            DownloadManager.STATUS_RUNNING,
//            DownloadManager.STATUS_PAUSED,
//            DownloadManager.STATUS_PENDING -> {
//
//              val progress = when {
//                total > 0 -> ((downloaded * 100L) / total).toInt()
//
//                // กรณีที่ยังไม่รู้ total → ให้ sync ตามระบบแบบนี้เลย
//                else -> {
//                  val timestamp =
//                    cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LAST_MODIFIED_TIMESTAMP))
//
//                  // mapping ให้ smooth เหมือน notification
//                  ((timestamp % 10000) / 100).toInt().coerceIn(0, 99)
//                }
//              }
//
//              if (progress != lastProgress) {
//                lastProgress = progress
//                _updateState.value = UpdateState.Downloading(progress)
//              }
//            }
//
//            DownloadManager.STATUS_SUCCESSFUL -> {
//              isDownloading = false
//            }
//
//            DownloadManager.STATUS_FAILED -> {
//              isDownloading = false
//              _updateState.value = UpdateState.Failed("การดาวน์โหลดล้มเหลว")
//            }
//          }
//        }
//
//        cursor?.close()
//        delay(200)
//      }
//    }
//  }
//
//
////  fun checkForUpdate() {
////    viewModelScope.launch {
////      _updateState.value = UpdateState.Checking
////      try {
////        val response = ApiRepository.getUpdate()
////        if (response.isSuccessful && response.body() != null) {
////          val updateInfo = response.body()!!.data
////          val currentVersionCode = BuildConfig.VERSION_CODE
////          if (updateInfo.versionCode > currentVersionCode) {
////            _updateState.value = UpdateState.UpdateAvailable(updateInfo)
////          } else {
////            _updateState.value = UpdateState.Idle
////          }
////        } else {
////          _updateState.value = UpdateState.Failed("ไม่สามารถตรวจสอบอัปเดตได้")
////        }
////      } catch (e: Exception) {
////        _updateState.value = UpdateState.Failed("เกิดข้อผิดพลาด: ${e.message}")
////      }
////    }
////  }
//
//  fun checkForUpdate() {
//    // --- ส่วนควบคุมการ Mock ---
//    val useMockData = true // <<< ตั้งเป็น true เพื่อทดสอบ, false เพื่อใช้งานจริง
//    // -----------------------
//
//    viewModelScope.launch {
//      _updateState.value = UpdateState.Checking
//      delay(1500) // จำลองการดีเลย์ของเน็ตเวิร์ก
//
//      if (useMockData) {
//        // --- ใช้ข้อมูล Mock ที่นี่ ---
//        val mockUpdateInfo = UpdateInfo(
//          versionCode = 999, // versionCode ที่สูงกว่าเวอร์ชันปัจจุบันแน่นอน
//          versionName = "2.0.0-mock",
//          apkUrl = "https://api.siamatic.co.th/etemp/media/app-release.apk",
//          changelog = "- ฟีเจอร์ใหม่สุดเจ๋ง\n- แก้ไขบั๊กสำคัญ\n- ปรับปรุง UI ให้สวยงามขึ้น"
//        )
//
//        // ตรวจสอบเวอร์ชันเหมือนเดิม แต่ใช้ข้อมูล Mock
//        val currentVersionCode = BuildConfig.VERSION_CODE
//        if (mockUpdateInfo.versionCode > currentVersionCode) {
//          _updateState.value = UpdateState.UpdateAvailable(mockUpdateInfo)
//        } else {
//          _updateState.value = UpdateState.Idle // กรณีทดสอบว่าถ้า versionCode ไม่ใหม่กว่า
//        }
//        // ---------------------------
//
//      } else {
//        // --- โค้ดเดิมสำหรับเรียก API จริง ---
//        try {
//          val response = ApiRepository.getUpdate()
//          if (response.isSuccessful && response.body() != null) {
//            val updateInfo = response.body()!!.data
//            val currentVersionCode = BuildConfig.VERSION_CODE
//            if (updateInfo.versionCode > currentVersionCode) {
//              _updateState.value = UpdateState.UpdateAvailable(updateInfo)
//            } else {
//              _updateState.value = UpdateState.Idle
//            }
//          } else {
//            _updateState.value = UpdateState.Failed("ไม่สามารถตรวจสอบอัปเดตได้")
//          }
//        } catch (e: Exception) {
//          _updateState.value = UpdateState.Failed("เกิดข้อผิดพลาด: ${e.message}")
//        }
//        // ----------------------------------
//      }
//    }
//  }
//
//  fun downloadUpdate(updateInfo: UpdateInfo) {
//    val application = getApplication<Application>()
//    val request = DownloadManager.Request(Uri.parse(updateInfo.apkUrl))
//      .setTitle("กำลังดาวน์โหลดอัปเดต LGS")
//      .setDescription("เวอร์ชัน ${updateInfo.versionName}")
//      .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
//      .setDestinationInExternalFilesDir(application, null, "update.apk") // หรือใช้ Cache dir
//
//    downloadId = downloadManager.enqueue(request)
//    _updateState.value = UpdateState.Downloading(0) // เริ่มต้นที่ 0%
//    trackDownloadProgress() // <<< เริ่มติดตาม Progress
//
//    application.registerReceiver(
//      onDownloadComplete,
//      IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
//      Context.RECEIVER_NOT_EXPORTED
//    )
//  }
//
//  fun installUpdate(context: Context, fileUri: Uri) {
//    val installIntent = Intent(Intent.ACTION_VIEW).apply {
//      setDataAndType(fileUri, "application/vnd.android.package-archive")
//      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//    }
//    context.startActivity(installIntent)
//    _updateState.value = UpdateState.Idle // Reset state
//  }
//
//  fun getBuildVersion(): String {
//    return buildVersionName
//  }
//}

package com.thanesgroup.lgs.data.viewModel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thanesgroup.lgs.BuildConfig
import com.thanesgroup.lgs.data.model.UpdateInfo
import com.thanesgroup.lgs.data.repositories.ApiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url
import java.io.File
import java.io.FileOutputStream

sealed class UpdateState {
  object Idle : UpdateState()
  object Checking : UpdateState()
  data class UpdateAvailable(val info: UpdateInfo) : UpdateState()
  data class Downloading(val progress: Int) : UpdateState()
  data class DownloadComplete(val fileUri: Uri) : UpdateState()
  data class Failed(val message: String) : UpdateState()
}

interface UpdateApi {
  @GET
  @Streaming
  suspend fun downloadApk(@Url url: String): retrofit2.Response<ResponseBody>
}

class UpdateViewModel(application: Application) : AndroidViewModel(application) {

  private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
  val updateState = _updateState.asStateFlow()
  private val buildVersionName = BuildConfig.VERSION_NAME

  private var downloadedFileUri: Uri? = null

  private val retrofit by lazy {
    Retrofit.Builder()
      .baseUrl("https://example.com/") // baseUrl จำเป็นแต่จะไม่ใช้
      .client(OkHttpClient.Builder().build())
      .addConverterFactory(GsonConverterFactory.create())
      .build()
  }

  /** -------------------------
   * ตรวจสอบเวอร์ชันใหม่
   * ------------------------- */
  fun checkForUpdate() {
    val useMockData = true // true เพื่อทดสอบ, false ใช้งานจริง
    viewModelScope.launch {
      _updateState.value = UpdateState.Checking
      kotlinx.coroutines.delay(1500)

      if (useMockData) {
        val mockUpdateInfo = UpdateInfo(
          versionCode = 999,
          versionName = "2.0.0-mock",
          apkUrl = "https://api.siamatic.co.th/etemp/media/app-release.apk",
          changelog = "- ฟีเจอร์ใหม่สุดเจ๋ง\n- แก้ไขบั๊กสำคัญ\n- ปรับปรุง UI ให้สวยงามขึ้น"
        )
        val currentVersionCode = BuildConfig.VERSION_CODE
        if (mockUpdateInfo.versionCode > currentVersionCode) {
          _updateState.value = UpdateState.UpdateAvailable(mockUpdateInfo)
        } else {
          _updateState.value = UpdateState.Idle
        }
      } else {
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
  }

  /** -------------------------
   * ดาวน์โหลด APK พร้อม track progress
   * ------------------------- */
  fun downloadUpdate(updateInfo: UpdateInfo) {
    viewModelScope.launch(Dispatchers.IO) {
      try {
        val api = retrofit.create(UpdateApi::class.java)
        val response = api.downloadApk(updateInfo.apkUrl)
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

          val fileUri = file.toUri()
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

  /** -------------------------
   * ติดตั้ง APK และลบไฟล์หลังติดตั้ง
   * ------------------------- */
  fun installUpdate(context: Context, fileUri: Uri) {
    val file = File(fileUri.path!!) // แปลง Uri → File
    val contentUri = FileProvider.getUriForFile(
      context,
      "${context.packageName}.fileprovider",
      file
    )

    val intent = Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
      data = contentUri
      flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
    }
    context.startActivity(intent)

    // ลบไฟล์หลังติดตั้ง
    file.delete()
  }

  /** -------------------------
   * คืนค่า version ปัจจุบัน
   * ------------------------- */
  fun getBuildVersion(): String {
    return buildVersionName
  }
}
