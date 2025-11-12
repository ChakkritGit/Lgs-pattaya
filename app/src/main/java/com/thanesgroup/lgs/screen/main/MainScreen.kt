package com.thanesgroup.lgs.screen.main

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.thanesgroup.lgs.data.viewModel.AuthState
import com.thanesgroup.lgs.data.viewModel.AuthViewModel
import com.thanesgroup.lgs.navigation.Routes
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
  navController: NavHostController,
  context: Context,
  authState: AuthState,
  authViewModel: AuthViewModel,
) {
  val scope = rememberCoroutineScope()

  Column() {
    Text(text = "Main Screen")
    Button(
      colors = ButtonDefaults.buttonColors(Color.Red),
      onClick = {
        scope.launch {
          authViewModel.logout(context)
          navController.navigate(Routes.Login.route) {
            popUpTo(Routes.Main.route) { inclusive = true }
          }
        }
      },
    ) {
      Text(text = "Log out", fontSize = 18.sp, color = Color.White)
    }
  }
}