package com.thanesgroup.lgs.remote.api.service

import com.thanesgroup.lgs.data.model.ApiResponse
import com.thanesgroup.lgs.data.model.CheckDrugModel
import com.thanesgroup.lgs.data.model.CheckUserModel
import com.thanesgroup.lgs.data.model.DispenseModel
import com.thanesgroup.lgs.data.model.LabelModel
import com.thanesgroup.lgs.data.model.LoginRequest
import com.thanesgroup.lgs.data.model.QrLoginRequest
import com.thanesgroup.lgs.data.model.ReceiveOrderModel
import com.thanesgroup.lgs.data.model.ReceiveOrderRequest
import com.thanesgroup.lgs.data.model.UpdateInfo
import com.thanesgroup.lgs.data.model.UserAuthData
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Streaming
import retrofit2.http.Url

interface ApiService {
  @POST("auth/login")
  suspend fun login(@Body request: LoginRequest): Response<ApiResponse<UserAuthData>>

  @POST("auth/qrlogin")
  suspend fun qrLogin(@Body request: QrLoginRequest): Response<ApiResponse<UserAuthData>>

  @GET("user/{id}")
  suspend fun checkTokenExpire(@Path("id") id: String): Response<ApiResponse<CheckUserModel>>

  @GET("prescription/order/{hn}")
  suspend fun dispense(@Path("hn") hn: String): Response<ApiResponse<DispenseModel>>

  @GET("prescription/dispensated/{hn}")
  suspend fun reorderDispense(@Path("hn") hn: String): Response<ApiResponse<DispenseModel>>

  @GET("prescription/narcotic/{drugcode}")
  suspend fun checkDrug(@Path("drugcode") drugcode: String): Response<ApiResponse<CheckDrugModel>>

  @GET("prescription/label/{hn}/{drugcode}")
  suspend fun getLabel(@Path("hn") hn: String, @Path("drugcode") drugcode: String): Response<ApiResponse<LabelModel>>

  @PATCH("prescription/receive/{binlo}")
  suspend fun receiveOrder(
    @Path("binlo") binlo: String?,
    @Body request: ReceiveOrderRequest
  ): Response<ApiResponse<ReceiveOrderModel>>

  @GET("v1/app/latest-update") // <<< แก้ไข Endpoint ให้ตรงกับ API ของคุณ
  suspend fun getLatestUpdateInfo(): Response<ApiResponse<UpdateInfo>>

  @GET
  @Streaming
  suspend fun downloadApk(@Url url: String): Response<ResponseBody>
}