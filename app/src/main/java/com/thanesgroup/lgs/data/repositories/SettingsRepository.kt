package com.thanesgroup.lgs.data.repositories

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.thanesgroup.lgs.data.model.DispenseOnModel
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
    val DISPENSEMODE = booleanPreferencesKey("dispense_mode")
    val ORDER_LABEL = stringPreferencesKey("order_label")
    val DISPENSE_DRUGCODE_DATA = stringPreferencesKey("dispense_drugCode")
  }

  private val gson = Gson()

  val hn: Flow<String> = context.dataStore.data.map { preferences ->
    preferences[PreferencesKeys.HN] ?: ""
  }

  val dispenseMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
    preferences[PreferencesKeys.DISPENSEMODE] ?: false
  }

  val dispenseDrugCodeData: Flow<DispenseOnModel?> = context.dataStore.data.map { preferences ->
    val json = preferences[PreferencesKeys.DISPENSE_DRUGCODE_DATA]
    if (json.isNullOrEmpty()) null
    else gson.fromJson(json, DispenseOnModel::class.java)
  }

  val orderLabelFlow: Flow<String?> =
    context.dataStore.data.map { pref -> pref[PreferencesKeys.ORDER_LABEL] }

  suspend fun saveHn(hn: String) {
    context.dataStore.edit { settings ->
      settings[PreferencesKeys.HN] = hn
    }
  }

  suspend fun saveDispenseMode(dispenseMode: Boolean) {
    context.dataStore.edit { settings ->
      settings[PreferencesKeys.DISPENSEMODE] = dispenseMode
    }
  }

  suspend fun saveDispenseDrugCode(model: DispenseOnModel?) {
    val json = gson.toJson(model)
    context.dataStore.edit { settings ->
      settings[PreferencesKeys.DISPENSE_DRUGCODE_DATA] = json
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