package com.thanesgroup.lgs.screen.main

import android.content.Context
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.thanesgroup.lgs.data.viewModel.AuthState
import com.thanesgroup.lgs.data.viewModel.AuthViewModel
import com.thanesgroup.lgs.navigation.BottomNavDestination
import com.thanesgroup.lgs.screen.dispense.DispenseScreen
import com.thanesgroup.lgs.screen.menu.MenuScreen
import com.thanesgroup.lgs.ui.theme.LgsBlue

@Composable
fun MainScreen(
  mainNavController: NavHostController,
  authState: AuthState,
  authViewModel: AuthViewModel,
  context: Context
) {
  val navController = rememberNavController()
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route

  val isDarkTheme = isSystemInDarkTheme()

  val navigationBarColor = if (isDarkTheme) {
    Color(0xFF121318)
  } else {
    Color(0xFFFAF8FF)
  }

  Scaffold(
    bottomBar = {
      NavigationBar(
        containerColor = navigationBarColor,
        modifier = Modifier.drawBehind {
          val strokeWidth = 1.dp.toPx()
          val y = 0f - (strokeWidth / 2)

          drawLine(
            color = Color.Gray.copy(alpha = 0.13f),
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = strokeWidth
          )
        }.height(85.dp)
      ) {
        BottomNavDestination.entries.forEach { destination ->
          val isSelected = currentRoute == destination.route
          NavigationBarItem(
            selected = isSelected,
            onClick = {
              navController.navigate(destination.route) {
                popUpTo(navController.graph.startDestinationId) { saveState = true }
                launchSingleTop = true
                restoreState = true
              }
            },
            icon = {
              val iconRes = if (isSelected) destination.selectedIcon else destination.unselectedIcon
              Icon(
                painter = painterResource(iconRes),
                contentDescription = destination.label,
                modifier = Modifier.size(28.dp)
              )
            },

//            label = {
//              Text(
//                destination.label,
//                fontWeight = FontWeight.Bold,
//                fontSize = 14.sp
//              )
//            },

            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = LgsBlue,
              selectedTextColor = LgsBlue,
              unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
              unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
              indicatorColor = Color.Transparent
            )
          )
        }
      }
    }
  ) { contentPadding ->
    NavHost(
      navController = navController,
      startDestination = BottomNavDestination.DISPENSE.route,
      enterTransition = { EnterTransition.None },
      exitTransition = { ExitTransition.None },
      popEnterTransition = { EnterTransition.None },
      popExitTransition = { ExitTransition.None },
      modifier = Modifier.padding(contentPadding)
    ) {
      composable(route = BottomNavDestination.DISPENSE.route) {
        DispenseScreen(
          navController = navController,
          context = context
        )
      }

      composable(route = BottomNavDestination.MENU.route) {
        MenuScreen(
          mainNavController = mainNavController,
          context = context,
          authViewModel = authViewModel,
          authState = authState
        )
      }
    }
  }
}