package com.thanesgroup.lgs.remote.api.service

import com.thanesgroup.lgs.data.model.ApiResponse
import com.thanesgroup.lgs.data.model.CheckDrugModel
import com.thanesgroup.lgs.data.model.CheckUserModel
import com.thanesgroup.lgs.data.model.DispenseModel
import com.thanesgroup.lgs.data.model.DispenseOnModel
import com.thanesgroup.lgs.data.model.LabelModel
import com.thanesgroup.lgs.data.model.LoginRequest
import com.thanesgroup.lgs.data.model.LogoutRequest
import com.thanesgroup.lgs.data.model.ManualDispenseOffRequest
import com.thanesgroup.lgs.data.model.ManualDispenseOnRequest
import com.thanesgroup.lgs.data.model.QrLoginRequest
import com.thanesgroup.lgs.data.model.ReceiveOrderModel
import com.thanesgroup.lgs.data.model.ReceiveOrderRequest
import com.thanesgroup.lgs.data.model.ReceiveRequest
import com.thanesgroup.lgs.data.model.UpdateInfo
import com.thanesgroup.lgs.data.model.UserAuthData
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
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

  @POST("auth/logout")
  suspend fun logout(@Body request: LogoutRequest): Response<ApiResponse<String>>

  @GET("user/{id}")
  suspend fun checkTokenExpire(@Path("id") id: String): Response<ApiResponse<CheckUserModel>>

  @GET("prescription/order/{hn}")
  suspend fun dispense(@Path("hn") hn: String): Response<ApiResponse<DispenseModel>>

  @POST("prescription/manual-on")
  suspend fun dispenseOn(@Body request: ManualDispenseOnRequest): Response<ApiResponse<DispenseOnModel>>

  @POST("prescription/manual-off")
  suspend fun dispenseOff(@Body request: ManualDispenseOffRequest): Response<ApiResponse<DispenseOnModel>>

  @DELETE("prescription/dispense/{hn}")
  suspend fun pauseDispense(@Path("hn") hn: String): Response<ApiResponse<DispenseModel>>

  @GET("prescription/dispensated/{hn}")
  suspend fun reorderDispense(@Path("hn") hn: String): Response<ApiResponse<DispenseModel>>

  @GET("prescription/narcotic/{drugcode}")
  suspend fun checkDrug(@Path("drugcode") drugcode: String): Response<ApiResponse<CheckDrugModel>>

  @POST("prescription/label")
  suspend fun getLabel(@Body request: ReceiveRequest): Response<ApiResponse<LabelModel>>

  @PATCH("prescription/receive/{binlo}")
  suspend fun receiveOrder(
    @Path("binlo") binlo: String?,
    @Body request: ReceiveOrderRequest
  ): Response<ApiResponse<ReceiveOrderModel>>

  @GET("upload/version/current")
  suspend fun getLatestUpdateInfo(): Response<ApiResponse<UpdateInfo>>

  @GET
  @Streaming
  suspend fun downloadApk(@Url url: String): Response<ResponseBody>
}