package com.thanesgroup.lgs.remote.api.service

import com.thanesgroup.lgs.data.model.ApiResponse
import com.thanesgroup.lgs.data.model.LoginRequest
import com.thanesgroup.lgs.data.model.QrLoginRequest
import com.thanesgroup.lgs.data.model.UserAuthData
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
  @POST("auth/login")
  suspend fun login(@Body request: LoginRequest): Response<ApiResponse<UserAuthData>>

  @POST("auth/qrlogin")
  suspend fun qrLogin(@Body request: QrLoginRequest): Response<ApiResponse<UserAuthData>>
}