package com.thanesgroup.lgs.data.model

data class UserAuthData(
  val id: String,
  val token: String
)

data class LoginRequest(val username: String, val password: String)
data class QrLoginRequest(val username: String)
