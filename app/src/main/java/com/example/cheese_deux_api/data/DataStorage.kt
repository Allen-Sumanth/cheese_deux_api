package com.example.cheese_deux_api.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

class DataStorage(private val context: Context) {
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "highScore")
        private val HIGH_SCORE = intPreferencesKey("highScore")
    }

    val highScoreFlow = context.dataStore.data.map {preferences ->
        preferences[HIGH_SCORE] ?: 0
    }

    suspend fun saveNewScore(newScore: Int) {
        context.dataStore.edit { preferences ->
            preferences[HIGH_SCORE] = newScore
        }
    }
}