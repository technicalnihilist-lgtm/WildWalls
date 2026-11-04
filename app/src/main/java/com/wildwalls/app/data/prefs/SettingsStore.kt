package com.wildwalls.app.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "wildwalls_settings")

data class AppSettings(
    val gridColumns: Int = 2,
    val dataSaver: Boolean = false,
    val dailyDate: String = "",
    val dailyPayload: String = ""
)

class SettingsStore(private val context: Context) {

    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            gridColumns = prefs[GRID_COLUMNS] ?: 2,
            dataSaver = prefs[DATA_SAVER] ?: false,
            dailyDate = prefs[DAILY_DATE] ?: "",
            dailyPayload = prefs[DAILY_PAYLOAD] ?: ""
        )
    }

    suspend fun setGridColumns(columns: Int) {
        context.dataStore.edit { it[GRID_COLUMNS] = columns.coerceIn(2, 3) }
    }

    suspend fun setDataSaver(enabled: Boolean) {
        context.dataStore.edit { it[DATA_SAVER] = enabled }
    }

    suspend fun setDaily(date: String, payload: String) {
        context.dataStore.edit {
            it[DAILY_DATE] = date
            it[DAILY_PAYLOAD] = payload
        }
    }

    private companion object {
        val GRID_COLUMNS = intPreferencesKey("grid_columns")
        val DATA_SAVER = booleanPreferencesKey("data_saver")
        val DAILY_DATE = stringPreferencesKey("daily_date")
        val DAILY_PAYLOAD = stringPreferencesKey("daily_payload")
        val LAST_SEEN = longPreferencesKey("last_seen")
    }
}
