package com.thanesgroup.lgs.screen.menu

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import com.thanesgroup.lgs.R
import com.thanesgroup.lgs.data.model.TokenDecodeModel
import com.thanesgroup.lgs.data.repositories.SettingsRepository
import com.thanesgroup.lgs.data.viewModel.AuthState
import com.thanesgroup.lgs.data.viewModel.AuthViewModel
import com.thanesgroup.lgs.data.viewModel.DataStoreViewModel
import com.thanesgroup.lgs.data.viewModel.DispenseViewModel
import com.thanesgroup.lgs.data.viewModel.UpdateState
import com.thanesgroup.lgs.navigation.MenuSubRoutes
import com.thanesgroup.lgs.navigation.Routes
import com.thanesgroup.lgs.ui.component.menu.SettingsMenuItem
import com.thanesgroup.lgs.ui.theme.LgsBlue
import com.thanesgroup.lgs.ui.theme.Red40
import com.thanesgroup.lgs.ui.theme.anuphanFamily
import com.thanesgroup.lgs.util.jwtDecode
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
  mainNavController: NavHostController,
  navController: NavHostController,
  authState: AuthState,
  updateState: UpdateState,
  authViewModel: AuthViewModel,
  dispenseViewModel: DispenseViewModel,
  dataStoreViewModel: DataStoreViewModel,
  context: Context
) {
  val scope = rememberCoroutineScope()
  val settings = remember { SettingsRepository.getInstance(context) }
  val payload = jwtDecode<TokenDecodeModel>(authState.token)
  var showLogoutDialog by remember { mutableStateOf(false) }
  var openToConfirmCancelDispenseDialog by remember { mutableStateOf(false) }
  var openConfirmSwitchDispenseDialog by remember { mutableStateOf(false) }
  val checked by dataStoreViewModel.getDispenseMode.collectAsState(initial = false)

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            "การตั้งค่า", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge
          )
        }, colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface,
          titleContentColor = MaterialTheme.colorScheme.onSurface
        )
      )
    }) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .verticalScroll(rememberScrollState())
        .background(MaterialTheme.colorScheme.surface)
    ) {
      Text(
        text = "ทั่วไป",
        style = MaterialTheme.typography.labelMedium,
        color = Color.Gray,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
      )

      SettingsMenuItem(
        icon = R.drawable.autorenew_24px, text = "อัปเดตแอพ", updateState = updateState, onClick = {
          navController.navigate(MenuSubRoutes.AppUpdate.route)
        })

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable {
            val newValue = !checked
            if (checked) {
              scope.launch {
                dataStoreViewModel.saveDispenseMode(newValue)
              }
            } else {
              openConfirmSwitchDispenseDialog = true
            }
          }
          .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          painter = painterResource(R.drawable.switch_access_2_24px),
          contentDescription = "switch_access_2_24px",
          modifier = Modifier.size(24.dp),
          tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
          verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)
        ) {
          Text(
            text = "สลับโหมดจัดยา", style = MaterialTheme.typography.bodyLarge
          )
          Text(
            text = if (!checked) "จัดตามใบสั่งยา" else "โหมดเปิดปิดไฟ",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.outline
          )
        }
        Switch(
          checked = checked,
          colors = SwitchDefaults.colors(LgsBlue),
          onCheckedChange = {
            scope.launch {
              if (checked) {
                dataStoreViewModel.saveDispenseMode(false)
              } else {
                openConfirmSwitchDispenseDialog = true
              }
            }
          }
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      Button(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(Red40),
        onClick = {
          if (dispenseViewModel.dispenseData != null) {
            openToConfirmCancelDispenseDialog = true
            showLogoutDialog = false
          } else {
            showLogoutDialog = true
          }
        },
      ) {
        Text(
          text = "ออกจากระบบ", style = MaterialTheme.typography.bodyLarge, color = Color.White
        )
      }
    }
  }

  if (openConfirmSwitchDispenseDialog) {
    Dialog(
      onDismissRequest = { openConfirmSwitchDispenseDialog = false },
      properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 34.dp)
          .border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline,
            shape = RoundedCornerShape(34.dp)
          ),
        shape = RoundedCornerShape(34.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
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
              text = "ยืนยันสลับโหมดจัดยา",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              fontFamily = anuphanFamily
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "หากยืนยันจะสลับไปโหมด เปิด/ปิด ไฟซึ่งไม่มีการตัดสต๊อก กรุณาตัดสต๊อกด้วยตนเอง!",
              style = MaterialTheme.typography.bodyMedium,
              fontFamily = anuphanFamily
            )
          }
          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = { openConfirmSwitchDispenseDialog = false },
              shape = CircleShape,
              modifier = Modifier.weight(1f),
              colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
              )
            ) {
              Text(
                text = "ปิด",
                fontFamily = anuphanFamily,
                style = MaterialTheme.typography.labelLarge
              )
            }

            Button(
              onClick = {
                scope.launch {
                  dataStoreViewModel.saveDispenseMode(true)
                }
                openConfirmSwitchDispenseDialog = false
              },
              colors = ButtonDefaults.buttonColors(containerColor = LgsBlue),
              shape = CircleShape,
              modifier = Modifier.weight(1f)
            ) {
              Text(
                text = "ยืนยัน",
                fontFamily = anuphanFamily,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White
              )
            }
          }
        }
      }
    }
  }

  if (openToConfirmCancelDispenseDialog) {
    Dialog(
      onDismissRequest = { openToConfirmCancelDispenseDialog = false },
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
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
              text = "ยังมีรายการจัดยาอยู่",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              fontFamily = anuphanFamily
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "คุณต้องการยกเลิกการจัดยาใช่หรือไม่?",
              style = MaterialTheme.typography.bodyMedium,
              fontFamily = anuphanFamily
            )
          }
          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = { openToConfirmCancelDispenseDialog = false },
              shape = CircleShape,
              modifier = Modifier.weight(1f),
              colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
              )
            ) {
              Text(
                text = "ปิด",
                fontFamily = anuphanFamily,
                style = MaterialTheme.typography.labelLarge
              )
            }

            Button(
              onClick = {
                if (payload == null) return@Button

                scope.launch {
                  val result = authViewModel.handleLogout(payload.color, payload.id)

                  if (result == 200) {
                    openToConfirmCancelDispenseDialog = false
                    settings.clearHn()
                    dispenseViewModel.dispenseData = null
                    authViewModel.logout(context)
                    mainNavController.navigate(Routes.Login.route) {
                      popUpTo(0) { inclusive = true }
                    }
                  } else if (result == 401) {
                    openToConfirmCancelDispenseDialog = false
                    authViewModel.logout(context)
                    mainNavController.navigate(Routes.Login.route) {
                      popUpTo(0) { inclusive = true }
                    }
                  }
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
              shape = CircleShape,
              modifier = Modifier.weight(1f)
            ) {
              Text(
                text = "ยืนยัน",
                fontFamily = anuphanFamily,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White
              )
            }
          }
        }
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
          .padding(horizontal = 52.dp)
          .border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline,
            shape = RoundedCornerShape(34.dp)
          ),
        shape = RoundedCornerShape(34.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
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
              text = "ยืนยันการออกจากระบบ",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              fontFamily = anuphanFamily
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "คุณต้องการออกจากระบบใช่หรือไม่?",
              style = MaterialTheme.typography.bodyMedium,
              fontFamily = anuphanFamily
            )
          }
          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                fontFamily = anuphanFamily,
                style = MaterialTheme.typography.labelLarge
              )
            }

            Button(
              onClick = {
                if (payload == null) return@Button

                scope.launch {
                  val result = authViewModel.handleLogout(payload.color, payload.id)

                  if (result == 200) {
                    showLogoutDialog = false
                    authViewModel.logout(context)
                    mainNavController.navigate(Routes.Login.route) {
                      popUpTo(0) { inclusive = true }
                    }
                  } else if (result == 401) {
                    showLogoutDialog = false
                    authViewModel.logout(context)
                    mainNavController.navigate(Routes.Login.route) {
                      popUpTo(0) { inclusive = true }
                    }
                  }
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = LgsBlue),
              shape = CircleShape,
              modifier = Modifier.weight(1f)
            ) {
              Text(
                text = "ยืนยัน",
                fontFamily = anuphanFamily,
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
