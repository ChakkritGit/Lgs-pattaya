package com.thanesgroup.lgs.screen.auth

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import com.thanesgroup.lgs.BuildConfig
import com.thanesgroup.lgs.R
import com.thanesgroup.lgs.data.repositories.ApiRepository
import com.thanesgroup.lgs.data.viewModel.AuthViewModel
import com.thanesgroup.lgs.data.viewModel.TokenHolder
import com.thanesgroup.lgs.data.viewModel.UpdateState
import com.thanesgroup.lgs.data.viewModel.UpdateViewModel
import com.thanesgroup.lgs.navigation.Routes
import com.thanesgroup.lgs.screen.dispense.DownloadProgress
import com.thanesgroup.lgs.ui.component.keyboard.Keyboard
import com.thanesgroup.lgs.ui.theme.LgsBlue
import com.thanesgroup.lgs.util.parseErrorMessage
import com.thanesgroup.lgs.util.parseExceptionMessage
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
  navController: NavHostController,
  authViewModel: AuthViewModel,
  updateViewModel: UpdateViewModel,
  context: Context,
  innerPadding: PaddingValues
) {
  val scope = rememberCoroutineScope()

  var username by rememberSaveable { mutableStateOf("") }
  var userpassword by rememberSaveable { mutableStateOf("") }
  var errorMessage by remember { mutableStateOf("") }
  var isLoading by remember { mutableStateOf(false) }
  var showUpdateDialog by remember { mutableStateOf(false) }
  val updateInfo by updateViewModel.updateInfo.collectAsState()
  val updateState by updateViewModel.updateState.collectAsState()

  val focusRequesterPassword = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current
  val buildVersionName = BuildConfig.VERSION_NAME

  val hideKeyboard = Keyboard.hideKeyboard()

  val completeFieldMessage = "กรุณากรอกข้อมูลให้ครบ!"
  val userDataInCompleteMessage = "เกิดข้อผิดพลาด!"

  fun handleLogin() {
    errorMessage = ""
    isLoading = true

    scope.launch {
      if (username.isEmpty() || userpassword.isEmpty()) {
        errorMessage = completeFieldMessage
        isLoading = false
        return@launch
      }

      try {
        hideKeyboard()
        val response = ApiRepository.login(username = username, userpassword = userpassword)

        if (response.isSuccessful) {
          val userData = response.body()?.data

          if (userData != null) {
            val token = userData.token
            authViewModel.login(context, token, userData)
            TokenHolder.token = token
            navController.navigate(Routes.Main.route) {
              popUpTo(Routes.Login.route) { inclusive = true }
            }
          } else {
            errorMessage = userDataInCompleteMessage
          }
        } else {
          val errorJson = response.errorBody()?.string()
          val message = parseErrorMessage(response.code(), errorJson)
          errorMessage = message
        }
      } catch (e: Exception) {
        errorMessage = parseExceptionMessage(e)
      } finally {
        isLoading = false
      }
    }
  }

  LaunchedEffect(updateState) {
    if (updateState is UpdateState.UpdateAvailable) {
      showUpdateDialog = true
    }
  }

  LaunchedEffect(errorMessage) {
    if (errorMessage.isNotEmpty()) {
      Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
      errorMessage = ""
    }
  }

  Box(
    modifier = Modifier
      .padding(innerPadding)
      .fillMaxSize()
      .drawWithCache {
        val topRightPath = Path().apply {
          moveTo(size.width * 0.7f, 0f)
          lineTo(size.width, 0f)
          lineTo(size.width, size.height * 0.13f)
          close()
        }
        val bottomLeftPath = Path().apply {
          moveTo(0f, size.height * 0.90f)
          lineTo(0f, size.height)
          lineTo(size.width * 0.25f, size.height)
          close()
        }

        onDrawBehind {
          drawPath(topRightPath, color = LgsBlue)
          drawPath(bottomLeftPath, color = LgsBlue)
        }
      }) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 32.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
//      Text(
//        text = "LGS",
//        fontSize = 42.sp,
//        fontWeight = FontWeight.Bold,
//        color = LgsBlue
//      )
      Image(
        painter = painterResource(id = R.drawable.lgs_logo),
        contentDescription = "lgs_logo",
        modifier = Modifier
          .fillMaxWidth(0.3f)
          .padding(bottom = 15.dp)
          .clip(shape = RoundedCornerShape(32.dp))
      )

      HorizontalDivider(
        modifier = Modifier
          .fillMaxWidth(0.9f)
          .padding(bottom = 25.dp),
        thickness = 3.dp,
        color = LgsBlue
      )

      Text(
        text = "ลงชื่อเข้าใช้งาน",
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.titleLarge
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "Light Guiding Station System",
        color = Color.Gray,
        style = MaterialTheme.typography.titleSmall
      )
      Spacer(modifier = Modifier.height(35.dp))

      OutlinedTextField(
        value = username,
        onValueChange = { username = it },
        modifier = Modifier
          .fillMaxWidth(),
        label = { Text("ชื่อผู้ใช้") },
        leadingIcon = {
          Icon(
            painter = painterResource(R.drawable.account_circle_24px),
            contentDescription = "account_circle_24px"
          )
        },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = LgsBlue,
          focusedLabelColor = LgsBlue,
          focusedLeadingIconColor = LgsBlue,
          unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
        ),
        keyboardActions = KeyboardActions(
          onNext = {
            focusRequesterPassword.requestFocus()
          }),
        keyboardOptions = KeyboardOptions.Default.copy(
          imeAction = ImeAction.Next
        ),
        shape = CircleShape
      )
      Spacer(modifier = Modifier.height(16.dp))

      OutlinedTextField(
        value = userpassword,
        onValueChange = { userpassword = it },
        modifier = Modifier
          .fillMaxWidth()
          .focusRequester(focusRequesterPassword),
        label = { Text("รหัสผ่าน") },
        leadingIcon = {
          Icon(
            painter = painterResource(R.drawable.password_24px),
            contentDescription = "account_circle_24px"
          )
        },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = LgsBlue,
          focusedLabelColor = LgsBlue,
          focusedLeadingIconColor = LgsBlue,
          unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
        ),
        keyboardActions = KeyboardActions(
          onDone = {
            keyboardController?.hide()
            handleLogin()
          }),
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Password, imeAction = ImeAction.Done
        ),
        shape = CircleShape,
        visualTransformation = PasswordVisualTransformation()

      )
      Spacer(modifier = Modifier.height(32.dp))

      Button(
        onClick = {
          if (isLoading) return@Button
          handleLogin()
        },
        modifier = Modifier
          .fillMaxWidth(),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = LgsBlue),
        enabled = !isLoading
      ) {
        if (!isLoading) {
          Text(
            text = "เข้าสู่ระบบ",
            fontWeight = FontWeight.Normal,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium
          )
        } else {
          CircularProgressIndicator(
            modifier = Modifier.size(24.dp), color = LgsBlue, strokeWidth = 2.dp
          )
        }
      }
      Spacer(modifier = Modifier.height(12.dp))

      OutlinedButton(
        onClick = {
          navController.navigate(Routes.QrLogin.route)
        },
        modifier = Modifier
          .fillMaxWidth(),
        shape = CircleShape,
        colors = ButtonDefaults.outlinedButtonColors(MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
      ) {
        Text(
          text = "เข้าสู่ระบบด้วย QrCode",
          fontWeight = FontWeight.Normal,
          style = MaterialTheme.typography.titleMedium
        )
      }
    }

    Text(
      text = "Version $buildVersionName",
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = 24.dp),
      color = MaterialTheme.colorScheme.outlineVariant,
      style = MaterialTheme.typography.labelMedium
    )

    if (showUpdateDialog) {
      val installPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
      ) {
        if (context.packageManager.canRequestPackageInstalls()) {
          updateInfo?.let { updateViewModel.downloadUpdate(it) }
        }
      }

      Dialog(
        onDismissRequest = { },
        properties = DialogProperties(
          dismissOnBackPress = false,
          dismissOnClickOutside = false,
          usePlatformDefaultWidth = false
        )
      ) {
        Card(
          modifier = Modifier
            .fillMaxWidth(0.85f)
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
            modifier = Modifier.padding(top = 14.dp, start = 14.dp, end = 14.dp, bottom = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "มีอัปเดตใหม่",
              fontWeight = FontWeight.Bold,
              style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(4.dp))

            Text(
              "เวอร์ชัน ${updateInfo?.version_name} พร้อมให้ติดตั้งแล้ว",
              style = MaterialTheme.typography.bodyMedium
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            Column(
              verticalArrangement = Arrangement.Top,
              horizontalAlignment = Alignment.Start
            ) {
              Text("มีอะไรใหม่:", fontWeight = FontWeight.SemiBold)
              Spacer(modifier = Modifier.height(8.dp))
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .heightIn(max = 200.dp)
                  .verticalScroll(rememberScrollState())
              ) {
                val rawChangelog = updateInfo?.changelog ?: "n/a"
                val formattedChangelog = rawChangelog.replace("\\n", "\n")

                Text(
                  text = formattedChangelog,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp, start = 8.dp, end = 8.dp, top = 8.dp),
              contentAlignment = Alignment.Center
            ) {
              when (updateState) {
                is UpdateState.UpdateAvailable -> {
                  Button(
                    onClick = {
                      if (updateInfo == null) return@Button

                      if (context.packageManager.canRequestPackageInstalls()) {
                        updateViewModel.downloadUpdate(updateInfo!!)
                      } else {
                        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                          data = "package:${context.packageName}".toUri()
                        }
                        installPermissionLauncher.launch(intent)
                      }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = LgsBlue)
                  ) {
                    Text("ดาวน์โหลด", color = Color.White)
                  }
                }

                is UpdateState.Downloading -> {
                  DownloadProgress(progress = (updateState as UpdateState.Downloading).progress)
                }

                is UpdateState.checkFile -> {
                  Text(
                    "กำลังเตรียมพร้อมสำหรับการติดตั้ง",
                    style = MaterialTheme.typography.labelLarge
                  )
                }

                is UpdateState.DownloadComplete -> {
                  Button(
                    onClick = {
                      updateViewModel.installUpdate(
                        context,
                        (updateState as UpdateState.DownloadComplete).fileUri
                      )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = LgsBlue)
                  ) {
                    Text("ติดตั้ง", color = Color.White)
                  }
                }

                else -> {}
              }
            }
          }
        }
      }
    }
  }
}
