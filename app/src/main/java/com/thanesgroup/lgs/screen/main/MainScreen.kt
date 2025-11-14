package com.thanesgroup.lgs.screen.main

import android.content.Context
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.thanesgroup.lgs.data.viewModel.AuthState
import com.thanesgroup.lgs.data.viewModel.AuthViewModel
import com.thanesgroup.lgs.navigation.BottomNavDestination
import com.thanesgroup.lgs.navigation.DISPENSE_GRAPH_ROUTE
import com.thanesgroup.lgs.navigation.MENU_GRAPH_ROUTE
import com.thanesgroup.lgs.navigation.MenuSubRoutes
import com.thanesgroup.lgs.screen.appUpdate.AppUpdateScreen
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
  val currentDestination = navBackStackEntry?.destination

  val isDarkTheme = isSystemInDarkTheme()
  val navigationBarColor = if (isDarkTheme) Color(0xFF121318) else Color(0xFFFAF8FF)

  val transitionSpec: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideInHorizontally(
      initialOffsetX = { fullWidth -> fullWidth },
      animationSpec = tween(300, easing = FastOutSlowInEasing)
    )
  }

  val popEnterSpec: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideInHorizontally(
      initialOffsetX = { fullWidth -> -fullWidth },
      animationSpec = tween(300, easing = FastOutSlowInEasing)
    )
  }

  val exitSpec: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutHorizontally(
      targetOffsetX = { fullWidth -> -fullWidth },
      animationSpec = tween(300, easing = FastOutSlowInEasing)
    )
  }

  val popExitSpec: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutHorizontally(
      targetOffsetX = { fullWidth -> fullWidth },
      animationSpec = tween(300, easing = FastOutSlowInEasing)
    )
  }

  Scaffold(
    bottomBar = {
      NavigationBar(
        containerColor = navigationBarColor,
        modifier = Modifier
          .drawBehind {
            val strokeWidth = 1.dp.toPx()
            val y = 0f - (strokeWidth / 2)
            drawLine(
              color = Color.Gray.copy(alpha = 0.13f),
              start = Offset(0f, y),
              end = Offset(size.width, y),
              strokeWidth = strokeWidth
            )
          }
          .height(85.dp)
      ) {
        BottomNavDestination.entries.forEach { destination ->
          val isSelected =
            currentDestination?.hierarchy?.any { it.route == destination.graphRoute } == true
          NavigationBarItem(
            selected = isSelected,
            onClick = {
              navController.navigate(destination.graphRoute) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
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
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = LgsBlue,
              unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
              indicatorColor = Color.Transparent
            )
          )
        }
      }
    }
  ) { contentPadding ->
    NavHost(
      navController = navController,
      startDestination = DISPENSE_GRAPH_ROUTE,
      enterTransition = transitionSpec,
      exitTransition = exitSpec,
      popEnterTransition = popEnterSpec,
      popExitTransition = popExitSpec,
//      enterTransition = { EnterTransition.None },
//      exitTransition = { ExitTransition.None },
//      popEnterTransition = { EnterTransition.None },
//      popExitTransition = { ExitTransition.None },
      modifier = Modifier.padding(contentPadding)
    ) {
      navigation(
        startDestination = BottomNavDestination.DISPENSE.startDestinationRoute,
        route = DISPENSE_GRAPH_ROUTE
      ) {
        composable(route = BottomNavDestination.DISPENSE.startDestinationRoute) {
          DispenseScreen(
            navController = navController,
            context = context
          )
        }
      }

      navigation(
        startDestination = BottomNavDestination.MENU.startDestinationRoute,
        route = MENU_GRAPH_ROUTE
      ) {
        composable(route = BottomNavDestination.MENU.startDestinationRoute) {
          MenuScreen(
            mainNavController = mainNavController,
            navController = navController,
            context = context,
            authViewModel = authViewModel,
            authState = authState
          )
        }
        composable(route = MenuSubRoutes.AppUpdate.route) {
          AppUpdateScreen(
            navController = navController
          )
        }
      }
    }
  }
}