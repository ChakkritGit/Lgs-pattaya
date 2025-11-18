package com.thanesgroup.lgs.screen.dispense

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import com.google.gson.Gson
import com.thanesgroup.lgs.R
import com.thanesgroup.lgs.data.model.DispenseModel
import com.thanesgroup.lgs.data.model.LabelModel
import com.thanesgroup.lgs.data.model.OrderModel
import com.thanesgroup.lgs.data.model.TokenDecodeModel
import com.thanesgroup.lgs.data.repositories.ApiRepository
import com.thanesgroup.lgs.data.repositories.SettingsRepository
import com.thanesgroup.lgs.data.viewModel.AuthState
import com.thanesgroup.lgs.data.viewModel.DispenseViewModel
import com.thanesgroup.lgs.data.viewModel.UpdateState
import com.thanesgroup.lgs.data.viewModel.UpdateViewModel
import com.thanesgroup.lgs.ui.component.BarcodeScanner
import com.thanesgroup.lgs.ui.component.keyboard.Keyboard
import com.thanesgroup.lgs.ui.theme.Blue80
import com.thanesgroup.lgs.ui.theme.LgsBlue
import com.thanesgroup.lgs.ui.theme.LgsGreen
import com.thanesgroup.lgs.ui.theme.LightBlue
import com.thanesgroup.lgs.ui.theme.LightGreen
import com.thanesgroup.lgs.ui.theme.LightRed
import com.thanesgroup.lgs.ui.theme.LightYellow
import com.thanesgroup.lgs.ui.theme.anuphanFamily
import com.thanesgroup.lgs.util.jwtDecode
import com.thanesgroup.lgs.util.parseErrorMessage
import com.thanesgroup.lgs.util.parseExceptionMessage
import com.thanesgroup.lgs.util.updateStatusBarColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DispenseScreen(
  dispenseViewModel: DispenseViewModel,
  authState: AuthState,
  contentPadding: PaddingValues,
  updateViewModel: UpdateViewModel,
  context: Context
) {
  val activity = LocalActivity.current as Activity
  val scope = rememberCoroutineScope()
  val keyboardController = LocalSoftwareKeyboardController.current
  val defaultColor = MaterialTheme.colorScheme.background
  val updateState by updateViewModel.updateState.collectAsState()
  val updateInfo by updateViewModel.updateInfo.collectAsState()
  val hideKeyboard = Keyboard.hideKeyboard()
  val payload = jwtDecode<TokenDecodeModel>(authState.token)
  var orderLabel by remember { mutableStateOf<LabelModel?>(null) }
  var openDialog by remember { mutableStateOf(false) }
  var openUserVerifyDialog by remember { mutableStateOf(false) }
  var isUserVerified by remember { mutableStateOf(false) }
  var user2: String? by remember { mutableStateOf(null) }
  var isCheckingLoading by remember { mutableStateOf(false) }
  var isVerifyLoading by remember { mutableStateOf(false) }
  var isReceived by remember { mutableStateOf(false) }
  var username by rememberSaveable { mutableStateOf("") }
  var userpassword by rememberSaveable { mutableStateOf("") }
  val focusRequesterPassword = remember { FocusRequester() }
  val hn = dispenseViewModel.dispenseData?.hn
  val settings = remember { SettingsRepository.getInstance(context) }
  val savedOrderLabelJson by settings.orderLabelFlow.collectAsState(initial = null)
  var showUpdateDialog by remember { mutableStateOf(false) }
  var showRetryGetLabelDialog by remember { mutableStateOf(false) }
  var isRetryGetLabelLoading by remember { mutableStateOf(false) }
  var showRetryReceiveDialog by remember { mutableStateOf(false) }
  var showSelectDrugDialog by remember { mutableStateOf(false) }
  var retryBarcode by remember { mutableStateOf("") }
  var scannedCodeText by remember { mutableStateOf("") }
  var filteredOrderData by remember { mutableStateOf<List<OrderModel>>(emptyList<OrderModel>()) }
  var filteredOrderDataObject by remember { mutableStateOf<OrderModel?>(null) }

  val pullRefreshState = rememberPullRefreshState(
    refreshing = dispenseViewModel.isLoading, onRefresh = {
      if (hn != null && hn.isNotEmpty()) {
        dispenseViewModel.handleReorderDispense(hn)
      }
    })

  fun handleUserVerify() {
    dispenseViewModel.errorMessage = ""
    isVerifyLoading = true

    scope.launch {
      if (username.isEmpty() || userpassword.isEmpty()) {
        dispenseViewModel.errorMessage = "กรุณากรอกข้อมูลให้ครบ"
        isVerifyLoading = false
        return@launch
      }

      try {
        hideKeyboard()
        val response = ApiRepository.login(username = username, userpassword = userpassword)

        if (response.isSuccessful) {
          val userData = response.body()?.data

          if (userData != null) {
            user2 = userData.name
            isUserVerified = true


            val findDrug =
              dispenseViewModel.dispenseData?.orders?.filter { it.f_orderitemcode == scannedCodeText }

            if (findDrug == null) return@launch

            when (findDrug.size) {
              0 -> {
                isCheckingLoading = false
              }

              1 -> {
                val labelData =
                  dispenseViewModel.handleGetLabel(findDrug[0].f_referenceCode, scannedCodeText)
                if (labelData != null) {
                  orderLabel = labelData
                  scope.launch {
                    settings.saveOrderLabel(Gson().toJson(orderLabel))
                  }
                  isCheckingLoading = false
                  openDialog = true
                } else {
                  isCheckingLoading = false
                  retryBarcode = scannedCodeText
                  showRetryGetLabelDialog = true
                }
              }

              else -> {
                isCheckingLoading = false
                showSelectDrugDialog = true
                filteredOrderData = findDrug
              }
            }
          } else {
            dispenseViewModel.errorMessage = "เกิดข้อผิดพลาด"
          }
        } else {
          val errorJson = response.errorBody()?.string()
          val message = parseErrorMessage(response.code(), errorJson)
          dispenseViewModel.errorMessage = message
        }
      } catch (e: Exception) {
        dispenseViewModel.errorMessage = parseExceptionMessage(e)
      } finally {
        isVerifyLoading = false
        openUserVerifyDialog = false
      }
    }
  }

  LaunchedEffect(payload, dispenseViewModel.dispenseData) {
    if (payload != null && dispenseViewModel.dispenseData != null) {
      val color = when (payload.color) {
        "1" -> LightRed
        "2" -> LightGreen
        "3" -> LightBlue
        else -> LightYellow
      }

      updateStatusBarColor(activity, color)
    } else {
      updateStatusBarColor(activity, defaultColor)
    }
  }

  LaunchedEffect(updateState) {
    if (updateState is UpdateState.UpdateAvailable) {
      showUpdateDialog = true
    }
  }

  LaunchedEffect(savedOrderLabelJson) {
    if (!savedOrderLabelJson.isNullOrEmpty()) {
      orderLabel = Gson().fromJson(savedOrderLabelJson, LabelModel::class.java)
      openDialog = true
    }
  }

  LaunchedEffect(dispenseViewModel.errorMessage) {
    if (dispenseViewModel.errorMessage.isNotEmpty()) {
      Toast.makeText(context, dispenseViewModel.errorMessage, Toast.LENGTH_SHORT).show()
      dispenseViewModel.errorMessage = ""
    }
  }

  DisposableEffect(Unit) {
    onDispose {
      updateStatusBarColor(activity, defaultColor)
    }
  }

  BarcodeScanner { scannedCode ->
    if (scannedCode.isEmpty()) return@BarcodeScanner

    if (dispenseViewModel.dispenseData == null) {
      dispenseViewModel.handleDispense(scannedCode)
    } else {
      scope.launch {
        if (orderLabel == null) {
          if (isCheckingLoading) return@launch

          isCheckingLoading = true

          val isChecked = dispenseViewModel.handleCheckNarcotic(scannedCode)
          scannedCodeText = scannedCode

          if (!isChecked) {
            val findDrug =
              dispenseViewModel.dispenseData?.orders?.filter { it.f_orderitemcode == scannedCode }

            if (findDrug == null) return@launch

            when (findDrug.size) {
              0 -> {
                isCheckingLoading = false
              }

              1 -> {
                val labelData =
                  dispenseViewModel.handleGetLabel(findDrug[0].f_referenceCode, scannedCode)
                if (labelData != null) {
                  orderLabel = labelData
                  scope.launch {
                    settings.saveOrderLabel(Gson().toJson(orderLabel))
                  }
                  isCheckingLoading = false
                  openDialog = true
                } else {
                  isCheckingLoading = false
                  retryBarcode = scannedCodeText
                  showRetryGetLabelDialog = true
                }
              }

              else -> {
                isCheckingLoading = false
                showSelectDrugDialog = true
                filteredOrderData = findDrug
              }
            }
          } else {
            isCheckingLoading = false

            if (!isUserVerified) {
              openUserVerifyDialog = true
            }
          }

        } else {
          if (dispenseViewModel.isReceiveLoading) return@launch

          isReceived = dispenseViewModel.handleReceive(
            orderLabel?.f_itemlocationno, orderLabel?.f_referenceCode, user2
          )

          if (isReceived) {
            openDialog = false
            isReceived = false
            orderLabel = null

            settings.clearOrderLabel()

            dispenseViewModel.handleReorderDispense(dispenseViewModel.dispenseData?.hn ?: "")
          } else {
            isReceived = false
            openDialog = false
            showRetryReceiveDialog = true
          }
        }
      }
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .pullRefresh(pullRefreshState)
  ) {

    if (dispenseViewModel.isLoading && dispenseViewModel.dispenseData == null) {
      Box(
        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
      ) {
        CircularProgressIndicator(
          modifier = Modifier.size(24.dp), color = LgsBlue, strokeWidth = 2.dp
        )
      }
    } else if (dispenseViewModel.dispenseData == null) {
      ScanPromptUI(contentPadding)
    } else {
      Box(
        modifier = Modifier
          .padding(contentPadding)
          .background(
            if (payload != null) {
              if (payload.color == "1") {
                LightRed
              } else if (payload.color == "2") {
                LightGreen
              } else if (payload.color == "3") {
                LightBlue
              } else {
                LightYellow
              }
            } else {
              MaterialTheme.colorScheme.background
            }
          )
      ) {
        Column(
          modifier = Modifier.fillMaxSize()
        ) {
          Spacer(modifier = Modifier.height(8.dp))

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(
                if (payload != null) {
                  if (payload.color == "1") {
                    LightRed
                  } else if (payload.color == "2") {
                    LightGreen
                  } else if (payload.color == "3") {
                    LightBlue
                  } else {
                    LightYellow
                  }
                } else {
                  MaterialTheme.colorScheme.background
                }
              ), contentAlignment = Alignment.Center
          ) {
            Text(
              text = if (payload != null) {
                if (payload.color == "1") {
                  "ตำแหน่งไฟสีแดง"
                } else if (payload.color == "2") {
                  "ตำแหน่งไฟสีเขียว"
                } else if (payload.color == "3") {
                  "ตำแหน่งไฟสีน้ำเงิน"
                } else {
                  "ตำแหน่งไฟสีเหลือง"
                }
              } else {
                "ไม่พบสี"
              },
              fontWeight = FontWeight.Bold,
              color = if (payload != null) {
                Color.White
              } else {
                MaterialTheme.colorScheme.onSurface
              },
              style = MaterialTheme.typography.titleLarge,
              modifier = Modifier.padding(vertical = 6.dp)
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          PatientInfoSection(data = dispenseViewModel.dispenseData!!)

          Spacer(modifier = Modifier.height(8.dp))

          DispenseListScreen(
            data = dispenseViewModel.dispenseData!!,
            dispenseViewModel = dispenseViewModel,
            scope = scope
          )
        }
      }
    }

    if (dispenseViewModel.dispenseData != null) {
      PullRefreshIndicator(
        refreshing = dispenseViewModel.isLoading,
        state = pullRefreshState,
        backgroundColor = defaultColor,
        contentColor = LgsBlue,
        modifier = Modifier
          .align(Alignment.TopCenter)
          .padding(top = 24.dp)
      )
    }
  }

  if (showSelectDrugDialog) {
    Dialog(
      onDismissRequest = { }, properties = DialogProperties(
        dismissOnBackPress = false, dismissOnClickOutside = false, usePlatformDefaultWidth = false
      )
    ) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp)
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
          LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            items(filteredOrderData) { order ->
              val isSelected = filteredOrderDataObject == order

              val borderColor = if (isSelected) {
                LgsBlue
              } else {
                MaterialTheme.colorScheme.outlineVariant
              }

              val backgroundSelected = if (isSelected) {
                LgsBlue.copy(alpha = 0.2f)
              } else {
                MaterialTheme.colorScheme.background
              }

              val borderWidth = if (isSelected) 2.dp else 1.dp

              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .border(borderWidth, borderColor, RoundedCornerShape(16.dp))
                  .clip(RoundedCornerShape(16.dp))
                  .clickable(
                    onClick = {
                      filteredOrderDataObject = order
                    })
                  .background(backgroundSelected)
                  .padding(vertical = 12.dp, horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Text(
                  text = "ชื่อยา: ${order.f_orderitemname}",
                  style = MaterialTheme.typography.bodyMedium,
                  color = Color.Gray,
                  maxLines = 2,
                  overflow = TextOverflow.Ellipsis
                )

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.Bottom
                ) {
                  Column(horizontalAlignment = Alignment.Start) {
                    Text(
                      text = "ใบสั่งยา : ${order.f_prescriptionnohis}",
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row {
                      Text(
                        text = "BinLo : ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                      )
                      Text(
                        text = order.f_itemlocationno,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                      )
                    }
                  }

                  Spacer(modifier = Modifier.width(16.dp))

                  Column(horizontalAlignment = Alignment.End) {
                    Text(
                      text = "จำนวน ${order.f_orderqty} ${order.f_orderunitdesc}",
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.SemiBold
                    )
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = {
                showSelectDrugDialog = false
                filteredOrderDataObject = null
                filteredOrderData = emptyList<OrderModel>()
              },
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
                  val labelData = dispenseViewModel.handleGetLabel(
                    filteredOrderDataObject!!.f_referenceCode,
                    filteredOrderDataObject!!.f_orderitemcode
                  )
                  if (labelData != null) {
                    orderLabel = labelData
                    scope.launch {
                      settings.saveOrderLabel(Gson().toJson(orderLabel))
                    }
                    showSelectDrugDialog = false
                    filteredOrderData = emptyList<OrderModel>()
                    openDialog = true
                  } else {
                    retryBarcode = filteredOrderDataObject!!.f_orderitemcode
                    showSelectDrugDialog = false
                    showRetryGetLabelDialog = true
                  }
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = LgsBlue),
              shape = CircleShape,
              modifier = Modifier.weight(1f),
              enabled = filteredOrderDataObject != null
            ) {
              Text(
                text = "จัดยา",
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

  if (showRetryGetLabelDialog) {
    Dialog(
      onDismissRequest = { }, properties = DialogProperties(
        dismissOnBackPress = false, dismissOnClickOutside = false, usePlatformDefaultWidth = false
      )
    ) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 32.dp)
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
            text = "จัดยาล้มเหลว",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge
          )
          Spacer(modifier = Modifier.height(4.dp))

          Text(
            "กรุณาลองใหม่อีกครั้ง", style = MaterialTheme.typography.bodyMedium
          )

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = { showRetryGetLabelDialog = false },
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
                  isRetryGetLabelLoading = true
                  val hn = dispenseViewModel.dispenseData!!.hn
                  val labelData = dispenseViewModel.handleGetLabel(hn, retryBarcode)

                  if (labelData != null) {
                    orderLabel = labelData
                    scope.launch {
                      settings.saveOrderLabel(Gson().toJson(orderLabel))
                    }
                    showRetryGetLabelDialog = false
                    openDialog = true
                    retryBarcode = ""
                  }

                  isRetryGetLabelLoading = false
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = LgsBlue),
              shape = CircleShape,
              modifier = Modifier.weight(1f),
              enabled = !isRetryGetLabelLoading
            ) {
              if (!isRetryGetLabelLoading) {
                Text(
                  text = "ลองอีกครั้ง",
                  fontFamily = anuphanFamily,
                  style = MaterialTheme.typography.labelLarge,
                  color = Color.White
                )
              } else {
                CircularProgressIndicator(
                  modifier = Modifier.size(24.dp), color = LgsBlue, strokeWidth = 2.dp
                )
              }
            }
          }
        }
      }
    }
  }

  if (showUpdateDialog) {
    val installPermissionLauncher = rememberLauncherForActivityResult(
      contract = ActivityResultContracts.StartActivityForResult()
    ) {
      if (context.packageManager.canRequestPackageInstalls()) {
        updateInfo?.let { updateViewModel.downloadUpdate(it) }
      }
    }

    Dialog(
      onDismissRequest = { }, properties = DialogProperties(
        dismissOnBackPress = false, dismissOnClickOutside = false, usePlatformDefaultWidth = false
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
            verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.Start
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
                  "กำลังเตรียมพร้อมสำหรับการติดตั้ง", style = MaterialTheme.typography.labelLarge
                )
              }

              is UpdateState.DownloadComplete -> {
                Button(
                  onClick = {
                    updateViewModel.installUpdate(
                      context, (updateState as UpdateState.DownloadComplete).fileUri
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

  if (openUserVerifyDialog) {
    Dialog(
      onDismissRequest = { }, properties = DialogProperties(
        dismissOnBackPress = false, dismissOnClickOutside = false, usePlatformDefaultWidth = false
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
            text = "ยืนยันตัวตน",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "สำหรับการจัดยาชนิดพิเศษ",
            color = Color.Gray,
            style = MaterialTheme.typography.titleSmall
          )

          Spacer(modifier = Modifier.height(24.dp))

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
                handleUserVerify()
              }),
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Password, imeAction = ImeAction.Done
            ),
            shape = CircleShape,
            visualTransformation = PasswordVisualTransformation()

          )
          Spacer(modifier = Modifier.height(28.dp))

          Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = { openUserVerifyDialog = false },
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
                if (isVerifyLoading) return@Button
                handleUserVerify()
              },
              modifier = Modifier.weight(1f),
              shape = CircleShape,
              colors = ButtonDefaults.buttonColors(containerColor = LgsBlue),
              enabled = !isVerifyLoading
            ) {
              if (!isVerifyLoading) {
                Text(
                  text = "ยืนยัน",
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
          }
        }
      }
    }
  }

  if (isCheckingLoading) {
    Dialog(
      onDismissRequest = { }, properties = DialogProperties(
        dismissOnBackPress = false, dismissOnClickOutside = false, usePlatformDefaultWidth = false
      )
    ) {
      Card(
        modifier = Modifier
          .fillMaxWidth(0.3f)
          .height(100.dp)
          .border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline,
            shape = RoundedCornerShape(34.dp)
          ),
        shape = RoundedCornerShape(34.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
      ) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(top = 14.dp, start = 14.dp, end = 14.dp, bottom = 10.dp),
          contentAlignment = Alignment.Center
        ) {
          CircularProgressIndicator(
            modifier = Modifier.size(24.dp), color = LgsBlue, strokeWidth = 2.dp
          )
        }
      }
    }
  }

  if (openDialog && orderLabel != null) {
    Dialog(
      onDismissRequest = { }, properties = DialogProperties(
        dismissOnBackPress = false, dismissOnClickOutside = false, usePlatformDefaultWidth = false
      )
    ) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 32.dp)
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
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(
              painter = painterResource(id = R.drawable.barcode_scanner_24px),
              contentDescription = "Scan icon",
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Scan ช่องยาเพื่อยืนยันการจัดยา",
              color = Color.Red,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Row {
              Text(
                text = "BinLo : ",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = orderLabel?.f_itemlocationno ?: "",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.Red
              )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
              text = orderLabel?.f_orderitemname ?: "ไม่พบชื่อยา",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = "จำนวน ${orderLabel?.f_orderqty ?: "0"} ${orderLabel?.f_orderunitdesc ?: ""}",
              style = MaterialTheme.typography.bodyMedium,
              color = Color.Gray
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = {
                scope.launch {
                  isReceived = dispenseViewModel.handleReceive(
                    orderLabel?.f_itemlocationno, orderLabel?.f_referenceCode, user2
                  )

                  if (isReceived) {
                    openDialog = false
                    isReceived = false
                    orderLabel = null

                    settings.clearOrderLabel()

                    dispenseViewModel.handleReorderDispense(
                      dispenseViewModel.dispenseData?.hn ?: ""
                    )
                  } else {
                    isReceived = false
                    openDialog = false
                    showRetryReceiveDialog = true
                  }
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = LgsBlue),
              shape = CircleShape,
              modifier = Modifier.weight(1f),
              enabled = !dispenseViewModel.isReceiveLoading
            ) {
              if (!dispenseViewModel.isReceiveLoading) {
                Text(
                  text = "ยืนยัน",
                  fontFamily = anuphanFamily,
                  style = MaterialTheme.typography.labelLarge,
                  color = Color.White
                )
              } else {
                CircularProgressIndicator(
                  modifier = Modifier.size(24.dp), color = LgsBlue, strokeWidth = 2.dp
                )
              }
            }
          }
        }
      }
    }
  }

  if (showRetryReceiveDialog && orderLabel != null) {
    Dialog(
      onDismissRequest = { }, properties = DialogProperties(
        dismissOnBackPress = false, dismissOnClickOutside = false, usePlatformDefaultWidth = false
      )
    ) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 32.dp)
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
            text = "รับยาล้มเหลว",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge
          )
          Spacer(modifier = Modifier.height(4.dp))

          Text(
            "กรุณาลองใหม่อีกครั้ง", style = MaterialTheme.typography.bodyMedium
          )

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = { showRetryReceiveDialog = false },
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
                  isRetryGetLabelLoading = true
                  isReceived = dispenseViewModel.handleReceive(
                    orderLabel?.f_itemlocationno, orderLabel?.f_referenceCode, user2
                  )

                  if (isReceived) {
                    showRetryReceiveDialog = false
                    isReceived = false
                    orderLabel = null

                    settings.clearOrderLabel()

                    dispenseViewModel.handleReorderDispense(
                      dispenseViewModel.dispenseData?.hn ?: ""
                    )
                  }

                  isRetryGetLabelLoading = false
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = LgsBlue),
              shape = CircleShape,
              modifier = Modifier.weight(1f),
              enabled = !isRetryGetLabelLoading
            ) {
              if (!isRetryGetLabelLoading) {
                Text(
                  text = "ลองอีกครั้ง",
                  fontFamily = anuphanFamily,
                  style = MaterialTheme.typography.labelLarge,
                  color = Color.White
                )
              } else {
                CircularProgressIndicator(
                  modifier = Modifier.size(24.dp), color = LgsBlue, strokeWidth = 2.dp
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun DownloadProgress(progress: Int) {
  Column(
    modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
  ) {
    LinearProgressIndicator(
      progress = { progress / 100f },
      modifier = Modifier.fillMaxWidth(),
      trackColor = Blue80,
      color = LgsBlue
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text("กำลังดาวน์โหลด... $progress%", style = MaterialTheme.typography.bodySmall)
  }
}

@Composable
private fun ScanPromptUI(contentPadding: PaddingValues) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(contentPadding), contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Text(
        text = "Light Guiding Station",
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.titleLarge
      )
      Text(
        text = "( LGS )",
        fontWeight = FontWeight.Normal,
        style = MaterialTheme.typography.titleLarge
      )
      Spacer(modifier = Modifier.height(22.dp))
      Text(
        text = "Scan Barcode / QRCode", style = MaterialTheme.typography.bodyLarge
      )
      Spacer(modifier = Modifier.height(42.dp))

      Box(
        modifier = Modifier
          .size(200.dp)
          .clip(CircleShape)
          .padding(4.dp)
          .border(BorderStroke(4.dp, LgsBlue), CircleShape), contentAlignment = Alignment.Center
      ) {
        Icon(
          painter = painterResource(id = R.drawable.lgs_scan),
          contentDescription = "Scan Barcode or QRCode",
          modifier = Modifier.size(100.dp),
          tint = MaterialTheme.colorScheme.onSurface
        )
      }
      Spacer(modifier = Modifier.height(42.dp))

      Text(
        text = "สแกน HN", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge
      )
      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = "จัดยาตามรายการยาในใบสั่งยาที่ทำการระบุ", style = MaterialTheme.typography.bodyMedium
      )
    }
  }
}

@Composable
private fun DispenseListScreen(
  data: DispenseModel, scope: CoroutineScope, dispenseViewModel: DispenseViewModel
) {
  var showPauseDispenseDialog by remember { mutableStateOf(false) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 8.dp)
      .clip(shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
      .background(MaterialTheme.colorScheme.background)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(8.dp)
    ) {
      LazyColumn(
        modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        items(data.orders) { order ->
          OrderItemCard(order = order)
        }

        item {
          Button(
            onClick = { showPauseDispenseDialog = true },
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 12.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = LgsBlue),
            enabled = !dispenseViewModel.isLoading
          ) {
            Text(
              text = "บันทึกการจัดยา",
              fontWeight = FontWeight.Normal,
              color = Color.White,
              style = MaterialTheme.typography.titleMedium
            )
          }
        }
      }
    }
  }

  if (showPauseDispenseDialog) {
    Dialog(
      onDismissRequest = { showPauseDispenseDialog = false },
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
              text = "ยืนยันการจัดยา",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              fontFamily = anuphanFamily
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "ยังมีรายการจัดยาที่ยังไม่เสร็จ ต้องการยืนยันการจัดยาหรือไม่?",
              style = MaterialTheme.typography.bodyMedium,
              fontFamily = anuphanFamily
            )
          }
          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = { showPauseDispenseDialog = false },
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
                  if (dispenseViewModel.dispenseData == null) return@launch

                  dispenseViewModel.handlePauseDispense(dispenseViewModel.dispenseData!!.hn)

                  showPauseDispenseDialog = false
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

@Composable
private fun PatientInfoSection(data: DispenseModel) {
  Box(
    modifier = Modifier.padding(horizontal = 8.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
        .clip(shape = RoundedCornerShape(20.dp))
        .background(MaterialTheme.colorScheme.background)
        .padding(horizontal = 16.dp, vertical = 14.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = "ใบสั่งยา: ${data.hn}",
          fontWeight = FontWeight.Bold,
          style = MaterialTheme.typography.bodyMedium
        )
        Text(
          text = "จำนวน ${data.orders.size} รายการ",
          fontWeight = FontWeight.Bold,
          color = LgsGreen,
          style = MaterialTheme.typography.bodyMedium
        )
      }
      Text(
        text = "ผู้ป่วย: ${data.patientName}",
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.bodyMedium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
private fun OrderItemCard(order: OrderModel) {
  val isDispensed = order.f_dispensestatus == "1"
  val binLocationColor = if (isDispensed) LgsGreen else Color.Red

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
      .clip(RoundedCornerShape(16.dp))
      .padding(vertical = 12.dp, horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    Text(
      text = "ชื่อยา: ${order.f_orderitemname}",
      style = MaterialTheme.typography.bodyMedium,
      color = Color.Gray,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis
    )

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Bottom
    ) {
      Column(horizontalAlignment = Alignment.Start) {
        Text(
          text = "ใบสั่งยา : ${order.f_prescriptionnohis}",
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row {
          Text(
            text = "BinLo : ",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = order.f_itemlocationno,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = binLocationColor
          )
        }
      }

      Spacer(modifier = Modifier.width(16.dp))

      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = "จำนวน ${order.f_orderqty} ${order.f_orderunitdesc}",
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(4.dp))

        StatusIndicator(isDispensed = isDispensed)
      }
    }
  }
}

@Composable
private fun StatusIndicator(isDispensed: Boolean) {
  val backgroundColor = if (isDispensed) Color(0xFFE6F8E9) else Color(0xFFE3F2FD)
  val textColor = if (isDispensed) LgsGreen else LgsBlue
  val iconRes = if (isDispensed) R.drawable.check_circle_24px else R.drawable.barcode_scanner_24px
  val text = if (isDispensed) "จัดยาแล้ว" else "Scan สตก.ยา"
  val isDark = isSystemInDarkTheme()

  Row(
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .background(if (isDark) Color(0xFF141417) else backgroundColor)
      .padding(horizontal = 8.dp, vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    Icon(
      painter = painterResource(id = iconRes),
      contentDescription = text,
      tint = textColor,
      modifier = Modifier.size(16.dp)
    )
    Text(
      text = text,
      color = textColor,
      style = MaterialTheme.typography.labelMedium,
      fontWeight = FontWeight.Bold
    )
  }
}