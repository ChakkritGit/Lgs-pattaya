package com.thanesgroup.lgs.data.repositories

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

  companion object {
    @Volatile
    private var INSTANCE: SettingsRepository? = null

    fun getInstance(context: Context): SettingsRepository {
      return INSTANCE ?: synchronized(this) {
        val instance = SettingsRepository(context.applicationContext)
        INSTANCE = instance
        instance
      }
    }
  }

  private object PreferencesKeys {
    val HN = stringPreferencesKey("hn")
    val SECOND_USER = stringPreferencesKey("second_user")
    val ORDER_LABEL = stringPreferencesKey("order_label")
  }

  val hn: Flow<String> = context.dataStore.data.map { preferences ->
      preferences[PreferencesKeys.HN] ?: ""
    }

  val orderLabelFlow: Flow<String?> =
    context.dataStore.data.map { pref -> pref[PreferencesKeys.ORDER_LABEL] }

  suspend fun saveHn(hn: String) {
    context.dataStore.edit { settings ->
      settings[PreferencesKeys.HN] = hn
    }
  }

  suspend fun saveSecondUser(name: String) {
    context.dataStore.edit { settings ->
      settings[PreferencesKeys.SECOND_USER] = name
    }
  }

  suspend fun saveOrderLabel(json: String) {
    context.dataStore.edit { prefs ->
      prefs[PreferencesKeys.ORDER_LABEL] = json
    }
  }

  suspend fun clearOrderLabel() {
    context.dataStore.edit { prefs ->
      prefs.remove(PreferencesKeys.ORDER_LABEL)
    }
  }

  suspend fun clearHn() {
    context.dataStore.edit { settings ->
      settings.remove(PreferencesKeys.HN)
    }
  }
}