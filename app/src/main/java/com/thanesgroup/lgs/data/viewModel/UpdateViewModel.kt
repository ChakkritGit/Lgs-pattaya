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
          apkUrl = "https://file.antutu.com/soft2/antutu-benchmark-v11-en.apk",
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
//  fun installUpdate(context: Context, fileUri: Uri) {
//    try {
//      // แปลง Uri ของ DownloadManager ให้เป็น contentUri ผ่าน FileProvider
//      val contentUri = FileProvider.getUriForFile(
//        context,
//        "${context.packageName}.fileprovider",
//        File(fileUri.path!!) // DownloadManager ให้ path จริงของไฟล์
//      )
//
//      val intent = Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
//        setData(contentUri)
//        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
//      }
//      context.startActivity(intent)
//
//    } catch (e: Exception) {
//      e.printStackTrace()
//      Toast.makeText(context, "ติดตั้งไม่สำเร็จ: ${e.message}", Toast.LENGTH_LONG).show()
//    }
//  }

  fun installUpdate(context: Context, fileUri: Uri) {
    // fileUri ที่ได้จาก DownloadComplete จะเป็น content://... หรือ file://...
    // เราต้องแปลงให้เป็น content URI ที่แชร์ผ่าน FileProvider เสมอ
    // เพื่อให้แอปอื่น (ตัวติดตั้งแพ็คเกจ) สามารถเข้าถึงไฟล์ได้อย่างปลอดภัย

    // 1. แปลง Uri ที่อาจจะเป็น file://... ให้เป็น File object ก่อน
    // ใช้ getApplication() เพื่อให้แน่ใจว่าเราได้ Context ที่ถูกต้อง
    val file = File(getApplication<Application>().externalCacheDir, "update.apk")

    // 2. สร้าง Content URI ผ่าน FileProvider
    val contentUri = FileProvider.getUriForFile(
      context,
      "${context.packageName}.fileprovider", // Authority ต้องตรงกับที่ประกาศใน AndroidManifest.xml
      file
    )

    // 3. สร้าง Intent สำหรับการติดตั้ง
    val intent = Intent(Intent.ACTION_VIEW).apply {
      // ใช้ ACTION_VIEW ซึ่งเป็นวิธีที่แนะนำสำหรับ Android 7.0 (Nougat) ขึ้นไป
      // พร้อมระบุประเภทข้อมูล (MIME Type) ให้ระบบรู้ว่านี่คือไฟล์สำหรับติดตั้ง
      setDataAndType(contentUri, "application/vnd.android.package-archive")

      // Flags ที่สำคัญ:
      // - FLAG_ACTIVITY_NEW_TASK: จำเป็นเมื่อเรียก startActivity จากนอก Activity (เช่น ViewModel)
      // - FLAG_GRANT_READ_URI_PERMISSION: ให้สิทธิ์ 'ชั่วคราว' แก่ตัวติดตั้งแพ็คเกจเพื่ออ่านไฟล์จาก contentUri ของเรา
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    // เริ่มกระบวนการติดตั้ง
    context.startActivity(intent)

    // 🚨 ข้อควรระวัง: ห้ามลบไฟล์ตรงนี้! (file.delete())
    // เพราะกระบวนการติดตั้งเกิดขึ้นแบบ Asynchronous ตัวติดตั้งของ Android
    // กำลังจะอ่านไฟล์นี้หลังจากที่เราเรียก startActivity()
    // หากเราลบไฟล์ทิ้งทันที ตัวติดตั้งจะหาไฟล์ไม่เจอและเกิดข้อผิดพลาด
  }


  /** -------------------------
   * คืนค่า version ปัจจุบัน
   * ------------------------- */
  fun getBuildVersion(): String {
    return buildVersionName
  }
}
