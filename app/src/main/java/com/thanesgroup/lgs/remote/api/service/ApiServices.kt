package com.thanesgroup.lgs.remote.api.service

import com.thanesgroup.lgs.data.model.ApiResponse
import com.thanesgroup.lgs.data.model.DispenseModel
import com.thanesgroup.lgs.data.model.LoginRequest
import com.thanesgroup.lgs.data.model.QrLoginRequest
import com.thanesgroup.lgs.data.model.UpdateInfo
import com.thanesgroup.lgs.data.model.UserAuthData
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
  @POST("auth/login")
  suspend fun login(@Body request: LoginRequest): Response<ApiResponse<UserAuthData>>

  @POST("auth/qrlogin")
  suspend fun qrLogin(@Body request: QrLoginRequest): Response<ApiResponse<UserAuthData>>

  @GET("prescription/order/{hn}")
  suspend fun dispense(@Path("hn") hn: String, ): Response<ApiResponse<DispenseModel>>

  @GET("v1/app/latest-update") // <<< แก้ไข Endpoint ให้ตรงกับ API ของคุณ
  suspend fun getLatestUpdateInfo(): Response<ApiResponse<UpdateInfo>>
}