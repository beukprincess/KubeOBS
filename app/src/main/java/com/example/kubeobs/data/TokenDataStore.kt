package com.example.kubeobs.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

object TokenDataStore {
    private val TOKEN_KEY = stringPreferencesKey("access_token")

    suspend fun saveToken(context: Context, token: String) {
        context.dataStore.edit { it[TOKEN_KEY] = token }
    }

    suspend fun getToken(context: Context): String? {
        return context.dataStore.data.first()[TOKEN_KEY]
    }

    suspend fun clearToken(context: Context) {
        context.dataStore.edit { it.remove(TOKEN_KEY) }
    }
}

val Context.dataStore by preferencesDataStore(name = "auth")