package com.thanesgroup.lgs.util

import android.content.Context
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.thanesgroup.lgs.data.viewModel.AuthViewModel
import com.thanesgroup.lgs.navigation.Routes

import kotlinx.coroutines.launch

fun handleUnauthorizedError(
  code: Int,
  context: Context,
  authViewModel: AuthViewModel,
  navController: NavHostController
) {
  if (code == 401) {
    authViewModel.viewModelScope.launch {
      authViewModel.logout(context)

      navController.navigate(Routes.Login.route) {
        popUpTo(0) { inclusive = true }
      }
    }
  }
}