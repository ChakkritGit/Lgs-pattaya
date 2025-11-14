package com.thanesgroup.lgs.navigation

import androidx.annotation.DrawableRes
import com.thanesgroup.lgs.R

const val DISPENSE_GRAPH_ROUTE = "dispense_graph"
const val MENU_GRAPH_ROUTE = "menu_graph"

enum class BottomNavDestination(
  val graphRoute: String,
  val startDestinationRoute: String,
  @DrawableRes val unselectedIcon: Int,
  @DrawableRes val selectedIcon: Int,
  val label: String
) {
  DISPENSE(
    graphRoute = DISPENSE_GRAPH_ROUTE,
    startDestinationRoute = "dispense_screen",
    unselectedIcon = R.drawable.barcode_24px,
    selectedIcon = R.drawable.barcode_scanner_24px,
    label = "จัดยา"
  ),
  MENU(
    graphRoute = MENU_GRAPH_ROUTE,
    startDestinationRoute = "menu_screen",
    unselectedIcon = R.drawable.menu_24px,
    selectedIcon = R.drawable.menu_fill_24px,
    label = "เมนู"
  )
}

sealed class MenuSubRoutes(val route: String) {
  object AppUpdate : MenuSubRoutes("app_update")
}