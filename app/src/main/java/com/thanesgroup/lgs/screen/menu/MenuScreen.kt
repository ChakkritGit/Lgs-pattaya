package com.thanesgroup.lgs.screen.menu

import android.content.Context
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.thanesgroup.lgs.R
import com.thanesgroup.lgs.data.viewModel.AuthState
import com.thanesgroup.lgs.data.viewModel.AuthViewModel
import com.thanesgroup.lgs.navigation.MenuSubRoutes
import com.thanesgroup.lgs.navigation.Routes
import com.thanesgroup.lgs.ui.component.menu.SettingsMenuItem
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
        title = { Text("การตั้งค่า", fontWeight = FontWeight.Bold) },
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
        text = "ทั่วไป",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
          .height(46.dp)
          .padding(horizontal = 16.dp),
        colors = ButtonDefaults.buttonColors(Color.Red),
        onClick = {
          showLogoutDialog = true
        },
      ) {
        Text(text = "ออกจากระบบ", fontSize = 18.sp, color = Color.White)
      }
    }
  }

  if (showLogoutDialog) {
    AlertDialog(
      containerColor = MaterialTheme.colorScheme.surface,
      modifier = Modifier
        .border(
          width = 1.dp,
          color = MaterialTheme.colorScheme.outlineVariant,
          shape = RoundedCornerShape(38.dp)
        )
        .clip(shape = RoundedCornerShape(38.dp)),
      text = {
        Column(
          verticalArrangement = Arrangement.spacedBy(6.dp),
          horizontalAlignment = Alignment.Start,
        ) {
          Text(
            text = "ยืนยันการออกจากระบบ",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = ibmpiexsansthailooped
          )
          Text(
            text = "คุณต้องการออกจากระบบใช่หรือไม่?",
            fontSize = 16.sp,
            fontFamily = ibmpiexsansthailooped
          )
        }
      },
      onDismissRequest = {
        showLogoutDialog = false
      },
      confirmButton = {
        Row(
          modifier = Modifier.fillMaxWidth()
        ) {
          Button(
            onClick = {
              scope.launch {
                authViewModel.logout(context)
                mainNavController.navigate(Routes.Login.route) {
                  popUpTo(0) { inclusive = true }
                }
              }
              showLogoutDialog = false
            },
            colors = ButtonDefaults.buttonColors(Color.Red),
            shape = CircleShape,
            modifier = Modifier
              .fillMaxWidth(0.5f)
              .height(50.dp)
          ) {
            Text(
              text = "ยืนยัน",
              fontFamily = ibmpiexsansthailooped,
              fontSize = 18.sp,
              color = Color.White
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          OutlinedButton(
            onClick = {
              showLogoutDialog = false
            },
            shape = CircleShape,
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp),
            colors = ButtonDefaults.outlinedButtonColors(MaterialTheme.colorScheme.surfaceContainerLow),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
          ) {
            Text(
              text = "ยกเลิก",
              fontFamily = ibmpiexsansthailooped,
              fontSize = 18.sp
            )
          }
        }
      },
      dismissButton = {}
    )
  }
}
