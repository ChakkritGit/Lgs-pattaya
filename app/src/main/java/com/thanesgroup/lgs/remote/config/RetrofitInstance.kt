package com.thanesgroup.lgs.remote.config

import com.thanesgroup.lgs.data.viewModel.TokenHolder
import com.thanesgroup.lgs.remote.api.service.ApiService
import com.thanesgroup.lgs.util.BaseUrl
import com.thanesgroup.lgs.util.BaseUrlOutSite
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class JsonHeaderInterceptor : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val req = chain.request().newBuilder()
      .addHeader("Accept", "application/json")
      .addHeader("Content-Type", "application/json")
      .build()

    return chain.proceed(req)
  }
}

class AuthInterceptor : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val token = TokenHolder.token.orEmpty()

    val builder = chain.request().newBuilder()
      .addHeader("Accept", "application/json")
      .addHeader("Content-Type", "application/json")

    if (token.isNotEmpty()) {
      builder.addHeader("Authorization", "Bearer $token")
    }

    return chain.proceed(builder.build())
  }
}

object RetrofitInstance {

  private val client = OkHttpClient.Builder()
    .addInterceptor(JsonHeaderInterceptor())
    .build()

  val api: ApiService by lazy {
    Retrofit.Builder()
      .baseUrl(BaseUrl)
      .client(client)
      .addConverterFactory(GsonConverterFactory.create())
      .build()
      .create(ApiService::class.java)
  }

  fun createApiWithAuth(): ApiService {
    val clientAuth = OkHttpClient.Builder()
      .addInterceptor(AuthInterceptor())
      .build()

    return Retrofit.Builder()
      .baseUrl(BaseUrl)
      .client(clientAuth)
      .addConverterFactory(GsonConverterFactory.create())
      .build()
      .create(ApiService::class.java)
  }
}

object RetrofitOutSiteInstance {

  private val client = OkHttpClient.Builder()
    .addInterceptor(JsonHeaderInterceptor())
    .build()

  val api: ApiService by lazy {
    Retrofit.Builder()
      .baseUrl(BaseUrlOutSite)
      .client(client)
      .addConverterFactory(GsonConverterFactory.create())
      .build()
      .create(ApiService::class.java)
  }
}
