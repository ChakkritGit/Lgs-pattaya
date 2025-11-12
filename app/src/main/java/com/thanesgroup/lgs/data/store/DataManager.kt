package com.thanesgroup.lgs.data.store

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.thanesgroup.lgs.data.model.UserAuthData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DATASTORE_NAME = "user_store"

private val Context.dataStore by preferencesDataStore(name = DATASTORE_NAME)

object DataManager {
  private val TOKEN_KEY = stringPreferencesKey("auth_token")
  private val USER_DATA_KEY = stringPreferencesKey("user_data")

  suspend fun saveToken(context: Context, token: String) {
    context.dataStore.edit { prefs ->
      prefs[TOKEN_KEY] = token
    }
  }

  fun getToken(context: Context): Flow<String> {
    return context.dataStore.data.map { prefs ->
      prefs[TOKEN_KEY] ?: ""
    }
  }

  suspend fun saveUserData(context: Context, userData: UserAuthData) {
    val json = Gson().toJson(userData)
    context.dataStore.edit { prefs ->
      prefs[USER_DATA_KEY] = json
    }
  }

  fun getUserData(context: Context): Flow<UserAuthData?> {
    return context.dataStore.data.map { prefs ->
      prefs[USER_DATA_KEY]?.let {
        try {
          Gson().fromJson(it, UserAuthData::class.java)
        } catch (_: Exception) {
          null
        }
      }
    }
  }

  suspend fun clearAll(context: Context) {
    context.dataStore.edit { it.clear() }
  }
}
