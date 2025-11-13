package com.thanesgroup.lgs.screen.auth

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.thanesgroup.lgs.R
import com.thanesgroup.lgs.data.repositories.ApiRepository
import com.thanesgroup.lgs.data.viewModel.AuthViewModel
import com.thanesgroup.lgs.data.viewModel.TokenHolder
import com.thanesgroup.lgs.navigation.Routes
import com.thanesgroup.lgs.ui.component.BarcodeScanner
import com.thanesgroup.lgs.ui.theme.LgsBlue
import com.thanesgroup.lgs.util.parseErrorMessage
import com.thanesgroup.lgs.util.parseExceptionMessage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginWithCodeScreen(
  navController: NavHostController,
  authViewModel: AuthViewModel,
  context: Context,
  innerPadding: PaddingValues
) {
  val scope = rememberCoroutineScope()

  var errorMessage by remember { mutableStateOf("") }
  var isLoading by remember { mutableStateOf(false) }
  val tooltipState = remember { TooltipState() }
  val positionProvider = TooltipDefaults.rememberTooltipPositionProvider()

  fun handleLogin(scannedCode: String) {
    if (scannedCode.isEmpty() || isLoading) {
      return
    }

    errorMessage = ""
    isLoading = true

    scope.launch {
      try {
        val response = ApiRepository.qrLogin(username = scannedCode)

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
            errorMessage = "เกิดข้อผิดพลาด"
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

  BarcodeScanner { scannedCode ->
    handleLogin(scannedCode)
  }

  Scaffold(
    modifier = Modifier.padding(innerPadding),
    topBar = {
      TooltipBox(
        positionProvider = positionProvider,
        tooltip = {
          PlainTooltip {
            Text("ย้อนกลับ")
          }
        },
        state = tooltipState
      ) {
        Box(
          modifier = Modifier
            .padding(14.dp)
            .shadow(elevation = 0.7.dp, shape = CircleShape)
            .size(42.dp)
            .clip(CircleShape)
            .border(
              width = 1.dp,
              color = MaterialTheme.colorScheme.outlineVariant,
              shape = CircleShape
            )
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable(onClick = { navController.popBackStack() }),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(R.drawable.arrow_back_ios_new_24px),
            contentDescription = "arrow_back_ios_new_24px",
            tint = MaterialTheme.colorScheme.onSurface
          )
        }
      }
    },
    containerColor = Color.Transparent
  ) { _ ->
    Box(
      modifier = Modifier
//        .padding(innerPadding)
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
//        Text(
//          text = "LGS",
//          fontSize = 42.sp,
//          fontWeight = FontWeight.Bold,
//          color = LgsBlue
//        )
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
          text = "ลงชื่อเข้าใช้งานด้วย QrCode",
          fontSize = 24.sp,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Light Guiding Station System",
          fontSize = 18.sp,
          color = Color.Gray
        )
        Spacer(modifier = Modifier.height(40.dp))

        if (isLoading) {
          CircularProgressIndicator(
            modifier = Modifier.size(32.dp),
            color = LgsBlue,
            strokeWidth = 2.dp
          )
        } else {
          Icon(
            modifier = Modifier.size(64.dp),
            tint = LgsBlue,
            painter = painterResource(R.drawable.barcode_scanner_24px),
            contentDescription = "barcode_scanner_24px"
          )
        }
      }
    }
  }
}