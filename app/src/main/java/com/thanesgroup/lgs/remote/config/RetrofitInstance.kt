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

object RetrofitInstance {
  val api: ApiService by lazy {
    Retrofit.Builder()
      .baseUrl(BaseUrl)
      .addConverterFactory(GsonConverterFactory.create())
      .build()
      .create(ApiService::class.java)
  }

  fun createApiWithAuth(): ApiService {
    val client = OkHttpClient.Builder()
      .addInterceptor(AuthInterceptor())
      .build()

    return Retrofit.Builder()
      .baseUrl(BaseUrl)
      .client(client)
      .addConverterFactory(GsonConverterFactory.create())
      .build()
      .create(ApiService::class.java)
  }
}

object RetrofitOutSiteInstance {
  val api: ApiService by lazy {
    Retrofit.Builder()
      .baseUrl(BaseUrlOutSite)
      .addConverterFactory(GsonConverterFactory.create())
      .build()
      .create(ApiService::class.java)
  }
}

class AuthInterceptor : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val token = TokenHolder.token.orEmpty()

    val request = if (token.isNotEmpty()) {
      chain.request().newBuilder()
        .addHeader("Authorization", "Bearer $token")
        .build()
    } else {
      chain.request()
    }

    return chain.proceed(request)
  }
}