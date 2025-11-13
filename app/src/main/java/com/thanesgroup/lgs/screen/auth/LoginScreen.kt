package com.thanesgroup.lgs.screen.auth

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.thanesgroup.lgs.R
import com.thanesgroup.lgs.data.repositories.ApiRepository
import com.thanesgroup.lgs.data.viewModel.AuthViewModel
import com.thanesgroup.lgs.data.viewModel.TokenHolder
import com.thanesgroup.lgs.navigation.Routes
import com.thanesgroup.lgs.ui.component.keyboard.Keyboard
import com.thanesgroup.lgs.ui.theme.BgWhite
import com.thanesgroup.lgs.ui.theme.LgsBlue
import com.thanesgroup.lgs.util.parseErrorMessage
import com.thanesgroup.lgs.util.parseExceptionMessage
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
  navController: NavHostController,
  authViewModel: AuthViewModel,
  context: Context
) {
  val scope = rememberCoroutineScope()

  var username by rememberSaveable { mutableStateOf("") }
  var userpassword by rememberSaveable { mutableStateOf("") }
  var errorMessage by remember { mutableStateOf("") }
  var isLoading by remember { mutableStateOf(false) }

  val focusRequesterPassword = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current

  val hideKeyboard = Keyboard.hideKeyboard()

  val completeFieldMessage = "กรุณากรอกข้อมูลให้ครบ!"
  val userDataInCompleteMessage = "ข้อมูลผู้ใช้ไม่สมบูรณ์!"

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

  LaunchedEffect(errorMessage) {
    if (errorMessage.isNotEmpty()) {
      Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
      errorMessage = ""
    }
  }

  Box(
    modifier = Modifier
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
      }
  ) {
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
          .fillMaxWidth(0.5f)
          .padding(bottom = 8.dp)
          .clip(shape = RoundedCornerShape(32.dp))
      )

      HorizontalDivider(
        modifier = Modifier
          .fillMaxWidth(0.9f)
          .padding(bottom = 40.dp), thickness = 4.dp, color = LgsBlue
      )

      Text(
        text = "ลงชื่อเข้าใช้งาน",
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "Light Guiding Station System",
        fontSize = 18.sp,
        color = Color.Gray
      )
      Spacer(modifier = Modifier.height(40.dp))

      OutlinedTextField(
        value = username,
        onValueChange = { username = it },
        modifier = Modifier.fillMaxWidth(),
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
          focusedLeadingIconColor = LgsBlue
        ),
        keyboardActions = KeyboardActions(
          onNext = {
            focusRequesterPassword.requestFocus()
          }),
        keyboardOptions = KeyboardOptions.Default.copy(
          imeAction = ImeAction.Next
        ),
        shape = RoundedCornerShape(100.dp)
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
          focusedLeadingIconColor = LgsBlue
        ),
        keyboardActions = KeyboardActions(
          onDone = {
            keyboardController?.hide()
            handleLogin()
          }),
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Password,
          imeAction = ImeAction.Done
        ),
        shape = RoundedCornerShape(100.dp),
        visualTransformation = PasswordVisualTransformation()

      )
      Spacer(modifier = Modifier.height(32.dp))

      Button(
        onClick = {
          if (isLoading) return@Button
          handleLogin()
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp),
        shape = RoundedCornerShape(100.dp),
        colors = ButtonDefaults.buttonColors(containerColor = LgsBlue),
        enabled = !isLoading
      ) {
        if (!isLoading) {
          Text(text = "เข้าสู่ระบบ", fontSize = 18.sp, fontWeight = FontWeight.Normal)
        } else {
          CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = LgsBlue,
            strokeWidth = 2.dp
          )
        }
      }
      Spacer(modifier = Modifier.height(16.dp))

      OutlinedButton(
        onClick = {
          navController.navigate(Routes.QrLogin.route)
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp),
        shape = RoundedCornerShape(100.dp),
        border = BorderStroke(1.dp, Color.LightGray)
      ) {
        Text(
          text = "เข้าสู่ระบบด้วย QrCode",
          fontSize = 18.sp,
          color = LgsBlue,
          fontWeight = FontWeight.Normal
        )
      }
    }
  }
}
