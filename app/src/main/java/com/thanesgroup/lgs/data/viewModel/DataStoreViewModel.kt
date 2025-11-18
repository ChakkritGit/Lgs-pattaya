package com.thanesgroup.lgs.data.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.thanesgroup.lgs.data.repositories.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DataStoreViewModel(
  private val settingsRepository: SettingsRepository
) : ViewModel() {

  val hn: StateFlow<String> = settingsRepository.hn
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = "Loading..."
    )

  val getDispenseMode = settingsRepository.dispenseMode.stateIn(
    viewModelScope,
    SharingStarted.Eagerly,
    false
  )

  fun saveDispenseMode(dispenseMode: Boolean) {
    viewModelScope.launch {
      settingsRepository.saveDispenseMode(dispenseMode)
    }
  }
}

class DataStoreViewModelFactory(private val repository: SettingsRepository) :
  ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(DataStoreViewModel::class.java)) {
      @Suppress("UNCHECKED_CAST")
      return DataStoreViewModel(repository) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}