package com.thanesgroup.lgs.data.repositories

import com.thanesgroup.lgs.data.model.ApiResponse
import com.thanesgroup.lgs.data.model.DispenseModel
import com.thanesgroup.lgs.data.model.LoginRequest
import com.thanesgroup.lgs.data.model.QrLoginRequest
import com.thanesgroup.lgs.data.model.UpdateInfo
import com.thanesgroup.lgs.data.model.UserAuthData
import com.thanesgroup.lgs.remote.config.RetrofitInstance
import retrofit2.Response

object ApiRepository {
  suspend fun login(username: String, userpassword: String): Response<ApiResponse<UserAuthData>> {
    val request = LoginRequest(username, userpassword)
    return RetrofitInstance.api.login(request)
  }

  suspend fun qrLogin(username: String): Response<ApiResponse<UserAuthData>> {
    val request = QrLoginRequest(username)
    return RetrofitInstance.api.qrLogin(request)
  }

  suspend fun dispense(hn: String): Response<ApiResponse<DispenseModel>> {
    return RetrofitInstance.createApiWithAuth().dispense(hn)
  }

  suspend fun getUpdate(): Response<ApiResponse<UpdateInfo>> {
    return RetrofitInstance.api.getLatestUpdateInfo()
  }
}