package com.thanesgroup.lgs.data.viewModel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.thanesgroup.lgs.data.model.DispenseModel
import com.thanesgroup.lgs.data.repositories.ApiRepository
import com.thanesgroup.lgs.data.repository.SettingsRepository
import com.thanesgroup.lgs.util.parseErrorMessage
import com.thanesgroup.lgs.util.parseExceptionMessage
import kotlinx.coroutines.launch

class DispenseViewModel(
  private val settingsRepository: SettingsRepository,
  application: Application
) : AndroidViewModel(application) {
  var dispenseData by mutableStateOf<DispenseModel?>(null)

  var isLoading by mutableStateOf(false)
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
            dispenseData = data
            settingsRepository.saveHn(hn)
          } else {
            settingsRepository.clearHn()
          }
        } else {
          val errorJson = response.errorBody()?.string()
          val errorApiMessage = parseErrorMessage(response.code(), errorJson)
          errorMessage = errorApiMessage
          settingsRepository.clearHn()
        }
      } catch (e: Exception) {
        val exceptionMessage = parseExceptionMessage(e)
        errorMessage = exceptionMessage
        settingsRepository.clearHn()
      } finally {
        isLoading = false
      }
    }
  }

  fun handleReceive(orderCode: String) {
    // เปิดไฟจัดยา
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
  private val application: Application
) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(DispenseViewModel::class.java)) {
      @Suppress("UNCHECKED_CAST")
      return DispenseViewModel(settingsRepository, application) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}
