package com.thanesgroup.lgs.navigation

import android.content.Context
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.thanesgroup.lgs.data.viewModel.AuthViewModel
import com.thanesgroup.lgs.data.viewModel.UpdateViewModel
import com.thanesgroup.lgs.screen.auth.LoginScreen
import com.thanesgroup.lgs.screen.auth.LoginWithCodeScreen
import com.thanesgroup.lgs.screen.main.MainScreen
import com.thanesgroup.lgs.screen.splashScreen.SplashScreen
import kotlinx.coroutines.delay

@Composable
fun AppNavigation(innerPadding: PaddingValues, navController: NavHostController, context: Context) {
  val authViewModel: AuthViewModel = viewModel()
  val updateViewModel: UpdateViewModel = viewModel()
  val authState by authViewModel.authState.collectAsState()

  LaunchedEffect(Unit) {
    authViewModel.initializeAuth(context)
  }

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

  NavHost(
    navController = navController,
    startDestination = Routes.Splash.route,
    enterTransition = transitionSpec,
    exitTransition = exitSpec,
    popEnterTransition = popEnterSpec,
    popExitTransition = popExitSpec
  ) {
    composable(route = Routes.Splash.route) {
      SplashScreen(innerPadding = innerPadding)
      LaunchedEffect(authState) {
        if (!authState.isLoading) {
          delay(2500L)

          val destination = if (authState.isAuthenticated) Routes.Main.route else Routes.Login.route
          navController.navigate(destination) {
            popUpTo(Routes.Splash.route) {
              inclusive = true
            }
          }
        }
      }
    }

    composable(route = Routes.Login.route) {
      LoginScreen(
        navController = navController,
        authViewModel = authViewModel,
        context = context,
        innerPadding = innerPadding
      )
    }

    composable(route = Routes.QrLogin.route) {
      LoginWithCodeScreen(
        navController = navController,
        authViewModel = authViewModel,
        context = context,
        innerPadding = innerPadding
      )
    }

    composable(route = Routes.Main.route) {
      MainScreen(
        context = context,
        authViewModel = authViewModel,
        authState = authState,
        mainNavController = navController,
        updateViewModel = updateViewModel
      )
    }
  }
}