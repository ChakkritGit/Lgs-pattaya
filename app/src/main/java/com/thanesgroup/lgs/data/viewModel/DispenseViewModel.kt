package com.thanesgroup.lgs.data.viewModel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.thanesgroup.lgs.data.model.DispenseModel
import com.thanesgroup.lgs.data.model.DispenseOnModel
import com.thanesgroup.lgs.data.model.LabelModel
import com.thanesgroup.lgs.data.repositories.ApiRepository
import com.thanesgroup.lgs.data.repositories.SettingsRepository
import com.thanesgroup.lgs.util.handleUnauthorizedError
import com.thanesgroup.lgs.util.parseErrorMessage
import com.thanesgroup.lgs.util.parseExceptionMessage
import kotlinx.coroutines.launch

class DispenseViewModel(
  private val settingsRepository: SettingsRepository,
  application: Application,
  private val authViewModel: AuthViewModel,
  private val navController: NavHostController,
  private val context: Context
) : AndroidViewModel(application) {
  var dispenseData by mutableStateOf<DispenseModel?>(null)
  var dispenseOnData by mutableStateOf<DispenseOnModel?>(null)

  var isLoading by mutableStateOf(false)
    private set

  var isReceiveLoading by mutableStateOf(false)
    private set

  var errorMessage by mutableStateOf("")

  fun handleDispense(hn: String) {
    if (hn.isBlank()) return
    isLoading = true

    viewModelScope.launch {
      try {
        val response = ApiRepository.dispense(hn)

        if (response.isSuccessful) {
          val data = response.body()?.data
          if (data != null) {
            if (data.orders.isNotEmpty()) {
              dispenseData = data
              settingsRepository.saveHn(hn)
            }
          } else {
            settingsRepository.clearHn()
          }
        } else {
          val errorJson = response.errorBody()?.string()
          val errorApiMessage = parseErrorMessage(response.code(), errorJson)
          errorMessage = errorApiMessage
          settingsRepository.clearHn()

          if (response.code() == 401) {
            handleUnauthorizedError(response.code(), context, authViewModel, navController)
          }
        }
      } catch (e: Exception) {
        if (e is retrofit2.HttpException && e.code() == 401) {
          handleUnauthorizedError(e.code(), context, authViewModel, navController)
        }

        val exceptionMessage = parseExceptionMessage(e)
        errorMessage = exceptionMessage
        settingsRepository.clearHn()
      } finally {
        isLoading = false
      }
    }
  }

  suspend fun handleDispenseOnManual(scannedCode: String): DispenseOnModel? {
    isLoading = true
    return try {
      val response = ApiRepository.dispenseOnManual(scannedCode)

      if (response.isSuccessful) {
        dispenseOnData = response.body()?.data
        response.body()?.data
      } else {
        val errorJson = response.errorBody()?.string()
        errorMessage = parseErrorMessage(response.code(), errorJson)

        if (response.code() == 401) {
          handleUnauthorizedError(response.code(), context, authViewModel, navController)
        }
        null
      }
    } catch (e: Exception) {
      if (e is retrofit2.HttpException && e.code() == 401) {
        handleUnauthorizedError(e.code(), context, authViewModel, navController)
      }

      errorMessage = parseExceptionMessage(e)
      null
    } finally {
      isLoading = false
    }
  }

  suspend fun handleDispenseOffManual(scannedCode: String): DispenseOnModel? {
    isLoading = true
    return try {
      val response = ApiRepository.dispenseOffManual(scannedCode)

      if (response.isSuccessful) {
        dispenseOnData = null
        response.body()?.data
      } else {
        val errorJson = response.errorBody()?.string()
        errorMessage = parseErrorMessage(response.code(), errorJson)

        if (response.code() == 401) {
          handleUnauthorizedError(response.code(), context, authViewModel, navController)
        }
        null
      }
    } catch (e: Exception) {
      if (e is retrofit2.HttpException && e.code() == 401) {
        handleUnauthorizedError(e.code(), context, authViewModel, navController)
      }

      errorMessage = parseExceptionMessage(e)
      null
    } finally {
      isLoading = false
    }
  }

  fun handlePauseDispense(hn: String) {
    if (hn.isBlank()) return
    isLoading = true

    viewModelScope.launch {
      try {
        val response = ApiRepository.pauseDispense(hn)

        if (response.isSuccessful) {
          dispenseData = null
          settingsRepository.clearHn()
          settingsRepository.clearOrderLabel()
        } else {
          val errorJson = response.errorBody()?.string()
          val errorApiMessage = parseErrorMessage(response.code(), errorJson)
          errorMessage = errorApiMessage
          settingsRepository.clearHn()

          if (response.code() == 401) {
            handleUnauthorizedError(response.code(), context, authViewModel, navController)
          }
        }
      } catch (e: Exception) {
        if (e is retrofit2.HttpException && e.code() == 401) {
          handleUnauthorizedError(e.code(), context, authViewModel, navController)
        }

        val exceptionMessage = parseExceptionMessage(e)
        errorMessage = exceptionMessage
        settingsRepository.clearHn()
      } finally {
        isLoading = false
      }
    }
  }

  fun handleReorderDispense(hn: String) {
    if (hn.isBlank()) return
    isLoading = true

    viewModelScope.launch {
      try {
        val response = ApiRepository.reorderDispense(hn)

        if (response.isSuccessful) {
          val data = response.body()?.data
          if (data != null && data.orders.isNotEmpty()) {
            dispenseData = data
            settingsRepository.saveHn(hn)
          } else {
            dispenseData = null
            settingsRepository.clearHn()
          }
        } else {
          val errorJson = response.errorBody()?.string()
          val errorApiMessage = parseErrorMessage(response.code(), errorJson)
          errorMessage = errorApiMessage
          settingsRepository.clearHn()

          if (response.code() == 401) {
            handleUnauthorizedError(response.code(), context, authViewModel, navController)
          }
        }
      } catch (e: Exception) {
        if (e is retrofit2.HttpException && e.code() == 401) {
          handleUnauthorizedError(e.code(), context, authViewModel, navController)
        }

        val exceptionMessage = parseExceptionMessage(e)
        errorMessage = exceptionMessage
        Log.d("UnAunthorized", exceptionMessage)
        settingsRepository.clearHn()
      } finally {
        isLoading = false
      }
    }
  }

  suspend fun handleCheckNarcotic(drugCode: String): Boolean {
    return try {
      val response = ApiRepository.checkDrug(drugCode)

      if (response.isSuccessful) {
        response.body()?.data?.isNarcotic ?: false
      } else {
        val errorJson = response.errorBody()?.string()
        errorMessage = parseErrorMessage(response.code(), errorJson)

        if (response.code() == 401) {
          handleUnauthorizedError(response.code(), context, authViewModel, navController)
        }
        false
      }
    } catch (e: Exception) {
      if (e is retrofit2.HttpException && e.code() == 401) {
        handleUnauthorizedError(e.code(), context, authViewModel, navController)
      }

      errorMessage = parseExceptionMessage(e)
      false
    }
  }

  suspend fun handleGetLabel(reference: String, drugCode: String): LabelModel? {
    return try {
      val response = ApiRepository.getLabel(reference, drugCode)

      if (response.isSuccessful) {
        response.body()?.data
      } else {
        val errorJson = response.errorBody()?.string()
        errorMessage = parseErrorMessage(response.code(), errorJson)

        if (response.code() == 401) {
          handleUnauthorizedError(response.code(), context, authViewModel, navController)
        }
        null
      }
    } catch (e: Exception) {
      if (e is retrofit2.HttpException && e.code() == 401) {
        handleUnauthorizedError(e.code(), context, authViewModel, navController)
      }

      errorMessage = parseExceptionMessage(e)
      null
    }
  }

  suspend fun handleReceive(binLo: String?, reference: String?, user: String?): Boolean {
    isReceiveLoading = true
    return try {
      val response = ApiRepository.receiveOrder(binLo, reference, user)

      if (response.isSuccessful) {
        val data = response.body()?.data
        data != null
      } else {
        val errorJson = response.errorBody()?.string()
        errorMessage = parseErrorMessage(response.code(), errorJson)

        if (response.code() == 401) {
          handleUnauthorizedError(response.code(), context, authViewModel, navController)
        }
        false
      }
    } catch (e: Exception) {
      if (e is retrofit2.HttpException && e.code() == 401) {
        handleUnauthorizedError(e.code(), context, authViewModel, navController)
      }
      errorMessage = parseExceptionMessage(e)
      false
    } finally {
      isReceiveLoading = false
    }
  }

  fun clearDispenseData() {
    viewModelScope.launch {
      dispenseData = null
      settingsRepository.clearHn()
    }
  }
}

class DispenseViewModelFactory(
  private val settingsRepository: SettingsRepository,
  private val application: Application,
  private val authViewModel: AuthViewModel,
  private val navController: NavHostController,
  private val context: Context
) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(DispenseViewModel::class.java)) {
      @Suppress("UNCHECKED_CAST")
      return DispenseViewModel(
        settingsRepository,
        application,
        authViewModel,
        navController,
        context
      ) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}
