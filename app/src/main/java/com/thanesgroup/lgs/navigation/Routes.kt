package com.thanesgroup.lgs.navigation

sealed class Routes(val route: String) {
  object Login: Routes(route = "login")
  object QrLogin: Routes(route = "qr_login")
  object Splash : Routes("splash")
  object Main: Routes(route = "main")
}