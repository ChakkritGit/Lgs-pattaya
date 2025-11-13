package com.thanesgroup.lgs.navigation

import androidx.annotation.DrawableRes
import com.thanesgroup.lgs.R

enum class BottomNavDestination(
  val route: String,
  @DrawableRes val unselectedIcon: Int,
  @DrawableRes val selectedIcon: Int,
  val label: String
) {
  DISPENSE(
    route = "dispense",
    unselectedIcon = R.drawable.barcode_24px,
    selectedIcon = R.drawable.barcode_scanner_24px,
    label = "จัดยา"
  ),
  MENU(
    route = "menu",
    unselectedIcon = R.drawable.menu_24px,
    selectedIcon = R.drawable.menu_fill_24px,
    label = "เมนู"
  )
}