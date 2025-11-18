package com.thanesgroup.lgs.data.model

data class UserAuthData(
  val id: String,
  val token: String,
  val name: String,
  val color: String
)

data class TokenDecodeModel(
  val id: String,
  val name: String,
  val color: String
)

data class LoginRequest(val username: String, val password: String)
data class QrLoginRequest(val username: String)
data class ReceiveOrderRequest(val reference: String?, val user2: String?)
data class LogoutRequest(val color: String, val id: String)
data class ReceiveRequest(val reference: String, val drugcode: String)
data class ManualDispenseOnRequest(val drugcode: String)
data class ManualDispenseOffRequest(val binlo: String)
