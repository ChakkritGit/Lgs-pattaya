package com.thanesgroup.lgs.screen.menu

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import com.thanesgroup.lgs.R
import com.thanesgroup.lgs.data.viewModel.AuthState
import com.thanesgroup.lgs.data.viewModel.AuthViewModel
import com.thanesgroup.lgs.navigation.MenuSubRoutes
import com.thanesgroup.lgs.navigation.Routes
import com.thanesgroup.lgs.ui.component.menu.SettingsMenuItem
import com.thanesgroup.lgs.ui.theme.LgsBlue
import com.thanesgroup.lgs.ui.theme.Red40
import com.thanesgroup.lgs.ui.theme.ibmpiexsansthailooped
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
  mainNavController: NavHostController,
  navController: NavHostController,
  authState: AuthState,
  authViewModel: AuthViewModel,
  context: Context
) {
  val scope = rememberCoroutineScope()
  var showLogoutDialog by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            "การตั้งค่า", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge
          )
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface,
          titleContentColor = MaterialTheme.colorScheme.onSurface
        )
      )
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .verticalScroll(rememberScrollState())
        .background(MaterialTheme.colorScheme.surface)
    ) {
      Text(
        text = "ทั่วไป", style = MaterialTheme.typography.labelMedium, color = Color.Gray,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
      )

      SettingsMenuItem(
        icon = R.drawable.autorenew_24px,
        text = "การอัปเดทซอฟแวร์",
        onClick = {
          navController.navigate(MenuSubRoutes.AppUpdate.route)
        }
      )

      Spacer(modifier = Modifier.height(24.dp))

      Button(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(Red40),
        onClick = {
          showLogoutDialog = true
        },
      ) {
        Text(
          text = "ออกจากระบบ", style = MaterialTheme.typography.bodyLarge, color = Color.White
        )
      }
    }
  }

  if (showLogoutDialog) {
    Dialog(
      onDismissRequest = { showLogoutDialog = false },
      properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 56.dp)
          .border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline,
            shape = RoundedCornerShape(34.dp)
          ),
        shape = RoundedCornerShape(34.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(
          modifier = Modifier.padding(top = 12.dp, start = 12.dp, end = 12.dp, bottom = 10.dp),
          horizontalAlignment = Alignment.Start
        ) {
          Column(
            modifier = Modifier.padding(top = 12.dp, start = 10.dp, end = 10.dp),
            horizontalAlignment = Alignment.Start
          ) {
            Text(
              text = "ยืนยันการออกจากระบบ", style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              fontFamily = ibmpiexsansthailooped
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "คุณต้องการออกจากระบบใช่หรือไม่?", style = MaterialTheme.typography.bodyMedium,
              fontFamily = ibmpiexsansthailooped
            )
          }
          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = { showLogoutDialog = false },
              shape = CircleShape,
              modifier = Modifier.weight(1f),
              colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
              )
            ) {
              Text(
                text = "ยกเลิก",
                fontFamily = ibmpiexsansthailooped,
                style = MaterialTheme.typography.labelLarge
              )
            }

            Button(
              onClick = {
                showLogoutDialog = false
                scope.launch {
                  authViewModel.logout(context)
                  mainNavController.navigate(Routes.Login.route) {
                    popUpTo(0) { inclusive = true }
                  }
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = LgsBlue),
              shape = CircleShape,
              modifier = Modifier.weight(1f)
            ) {
              Text(
                text = "ยืนยัน",
                fontFamily = ibmpiexsansthailooped,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White
              )
            }
          }
        }
      }
    }
  }
}
