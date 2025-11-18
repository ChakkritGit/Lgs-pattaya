package com.thanesgroup.lgs.navigation

import androidx.annotation.DrawableRes
import com.thanesgroup.lgs.R

sealed class Routes(val route: String) {
  object Login : Routes(route = "login")
  object QrLogin : Routes(route = "qr_login")
  object Splash : Routes("splash")
  object Main : Routes(route = "main")
}

// for dispense turn on/off light screen
enum class DispenseRoutes(
  val route: String,
  @DrawableRes val unselectedIcon: Int,
  @DrawableRes val selectedIcon: Int,
  val label: String
) {
  turnOn(
    route = "turn_on",
    unselectedIcon = R.drawable.lightbulb_24px,
    selectedIcon = R.drawable.lightbulb_fill_24px,
    label = "เปิดไฟ"
  ),
  turnOff(
    route = "turn_off",
    unselectedIcon = R.drawable.light_off_24px,
    selectedIcon = R.drawable.light_off_fill_24px,
    label = "ปิดไฟ"
  )
}