package com.thanesgroup.lgs.navigation

import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.thanesgroup.lgs.data.model.TokenDecodeModel
import com.thanesgroup.lgs.data.repositories.ApiRepository
import com.thanesgroup.lgs.data.repositories.SettingsRepository
import com.thanesgroup.lgs.data.viewModel.AuthState
import com.thanesgroup.lgs.data.viewModel.AuthViewModel
import com.thanesgroup.lgs.data.viewModel.DataStoreViewModel
import com.thanesgroup.lgs.data.viewModel.DataStoreViewModelFactory
import com.thanesgroup.lgs.data.viewModel.DispenseViewModel
import com.thanesgroup.lgs.data.viewModel.DispenseViewModelFactory
import com.thanesgroup.lgs.data.viewModel.UpdateViewModel
import com.thanesgroup.lgs.screen.auth.LoginScreen
import com.thanesgroup.lgs.screen.auth.LoginWithCodeScreen
import com.thanesgroup.lgs.screen.main.MainScreen
import com.thanesgroup.lgs.screen.splashScreen.SplashScreen
import com.thanesgroup.lgs.util.handleUnauthorizedError
import com.thanesgroup.lgs.util.jwtDecode
import com.thanesgroup.lgs.util.parseErrorMessage
import com.thanesgroup.lgs.util.parseExceptionMessage
import com.thanesgroup.lgs.util.updateStatusBarColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(innerPadding: PaddingValues, navController: NavHostController, context: Context) {
  val scope = rememberCoroutineScope()
  val activity = LocalActivity.current as Activity
  val application = context.applicationContext as Application
  val authViewModel: AuthViewModel = viewModel()
  val updateViewModel: UpdateViewModel = viewModel()
  val dataStoreViewModel: DataStoreViewModel = viewModel(
    factory = DataStoreViewModelFactory(SettingsRepository.getInstance(context))
  )

  val dispenseViewModel: DispenseViewModel = viewModel(
    factory = DispenseViewModelFactory(
      settingsRepository = SettingsRepository.getInstance(context),
      context = context,
      authViewModel = authViewModel,
      navController = navController,
      application = application
    )
  )

  val authState by authViewModel.authState.collectAsState()
  val storedHn by dataStoreViewModel.hn.collectAsState()
  val defaultColor = MaterialTheme.colorScheme.background

  fun handleCheckTokenExpire(authStateParam: AuthState) {
    scope.launch {
      try {
        val id = jwtDecode<TokenDecodeModel>(authStateParam.token) ?: return@launch

        val response = ApiRepository.checkTokenExpire(id.id)

        if (response.isSuccessful) {
          val data = response.body()?.data
          if (data != null) {
            Log.d("AuthSuccess", data.f_userfullname ?: "n/a")
          }
        } else {
          val errorJson = response.errorBody()?.string()
          val errorApiMessage = parseErrorMessage(response.code(), errorJson)
          Log.e("AuthFail: ", errorApiMessage)

          if (response.code() == 401) {
            handleUnauthorizedError(response.code(), context, authViewModel, navController)
          }
        }
      } catch (e: Exception) {
        if (e is retrofit2.HttpException && e.code() == 401) {
          handleUnauthorizedError(e.code(), context, authViewModel, navController)
        }

        val exceptionMessage = parseExceptionMessage(e)
        Log.e("AuthFail: ", exceptionMessage)
      }
    }
  }

  LaunchedEffect(dispenseViewModel.dispenseData) {
    if (dispenseViewModel.dispenseData == null) {
      updateStatusBarColor(activity, defaultColor)
    }
  }

  LaunchedEffect(Unit) {
    authViewModel.initializeAuth(context)
  }

  LaunchedEffect(authState) {
    handleCheckTokenExpire(authState)
  }

  LaunchedEffect(Unit) {
    updateViewModel.checkForUpdate()
  }

  LaunchedEffect(storedHn) {
    if (dispenseViewModel.dispenseData == null && storedHn.isNotEmpty() && storedHn != "Loading...") {
      dispenseViewModel.handleReorderDispense(storedHn)
    }
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
        updateViewModel = updateViewModel,
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
        updateViewModel = updateViewModel,
        dispenseViewModel = dispenseViewModel
      )
    }
  }
}