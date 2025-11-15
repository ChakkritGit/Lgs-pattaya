package com.thanesgroup.lgs.util

import android.util.Base64
import com.google.gson.Gson

inline fun <reified T> jwtDecode(token: String?): T? {
  return try {
    val parts = token?.split(".")
    if (parts?.size != 3) return null

    val payload = parts[1]
    val decoded = Base64.decode(payload, Base64.URL_SAFE)
    val json = String(decoded, Charsets.UTF_8)

    Gson().fromJson(json, T::class.java)
  } catch (e: Exception) {
    null
  }
}
