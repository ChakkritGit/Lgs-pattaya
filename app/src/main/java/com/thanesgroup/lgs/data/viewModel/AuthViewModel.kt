package com.thanesgroup.lgs.data.viewModel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thanesgroup.lgs.data.model.UserAuthData
import com.thanesgroup.lgs.data.repositories.ApiRepository
import com.thanesgroup.lgs.data.store.DataManager
import com.thanesgroup.lgs.util.parseErrorMessage
import com.thanesgroup.lgs.util.parseExceptionMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class AuthState(
  val isLoading: Boolean = true,
  val token: String? = null,
  val userData: UserAuthData? = null,
  val isAuthenticated: Boolean = false,
  val error: String? = null
)

object TokenHolder {
  var token: String? = null
}

class AuthViewModel : ViewModel() {
  private val _authState = MutableStateFlow(AuthState())
  val authState: StateFlow<AuthState> = _authState.asStateFlow()
  var isLoading by mutableStateOf(false)
    private set
  var errorMessage by mutableStateOf("")

  fun initializeAuth(context: Context) {
    viewModelScope.launch {
      try {
        _authState.value = _authState.value.copy(isLoading = true, error = null)

        val token = DataManager.getToken(context).first()
        TokenHolder.token = token
        val userData = if (token.isNotEmpty()) {
          DataManager.getUserData(context).first()
        } else {
          null
        }

        _authState.value = _authState.value.copy(
          isLoading = false,
          token = token,
          userData = userData,
          isAuthenticated = token.isNotEmpty() && userData != null
        )
      } catch (e: Exception) {
        _authState.value = _authState.value.copy(
          isLoading = false,
          error = e.message
        )
      }
    }
  }

  fun login(context: Context, token: String, userData: UserAuthData) {
    viewModelScope.launch {
      try {
        DataManager.saveToken(context, token)
        DataManager.saveUserData(context, userData)

        _authState.value = _authState.value.copy(
          token = token,
          userData = userData,
          isAuthenticated = true,
          error = null
        )
      } catch (e: Exception) {
        _authState.value = _authState.value.copy(
          error = e.message
        )
      }
    }
  }

  fun logout(context: Context) {
    viewModelScope.launch {
      try {
        DataManager.clearAll(context)

        _authState.value = AuthState(
          isLoading = false,
          token = null,
          userData = null,
          isAuthenticated = false
        )
      } catch (e: Exception) {
        _authState.value = _authState.value.copy(
          error = e.message
        )
      }
    }
  }

  suspend fun handleLogout(color: String, id: String): Int {
    if (color.isEmpty() && id.isEmpty()) return 404

    return try {
      val response = ApiRepository.logout(color, id)

      if (response.isSuccessful) {
        200
      } else {
        val errorJson = response.errorBody()?.string()
        errorMessage = parseErrorMessage(response.code(), errorJson)

        if (response.code() == 401) {
          401
        }
        400
      }
    } catch (e: Exception) {
      if (e is retrofit2.HttpException && e.code() == 401) {
        401
      }

      errorMessage = parseExceptionMessage(e)
      500
    }
  }

  fun updateUserData(context: Context, userData: UserAuthData) {
    viewModelScope.launch {
      try {
        DataManager.saveUserData(context, userData)

        _authState.value = _authState.value.copy(
          userData = userData
        )
      } catch (e: Exception) {
        _authState.value = _authState.value.copy(
          error = e.message
        )
      }
    }
  }

  fun clearError() {
    _authState.value = _authState.value.copy(error = null)
  }
}