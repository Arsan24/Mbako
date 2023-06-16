package com.bangkit.capstone

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = Constanta.preferenceName)


class SettingPreferences private constructor(private val dataStore: DataStore<Preferences>) {
    private val token = stringPreferencesKey(Constanta.UserPreferences.UserToken.name)
    private val username = stringPreferencesKey(Constanta.UserPreferences.UserName.name)
    private val contact = stringPreferencesKey(Constanta.UserPreferences.UserContact.name)
    private val email = stringPreferencesKey(Constanta.UserPreferences.UserEmail.name)
    private val lastLogin = stringPreferencesKey(Constanta.UserPreferences.UserLastLogin.name)

    fun getUserToken(): Flow<String> = dataStore.data.map { it[token] ?: Constanta.preferenceDefaultValue }
    fun getUserName(): Flow<String> = dataStore.data.map { it[username] ?: Constanta.preferenceDefaultValue }
    fun getUserContact(): Flow<String> = dataStore.data.map { it[contact] ?: Constanta.preferenceDefaultValue }
    fun getUserEmail(): Flow<String> = dataStore.data.map { it[email] ?: Constanta.preferenceDefaultValue }
    fun getUserLastLogin(): Flow<String> = dataStore.data.map { it[lastLogin] ?: Constanta.preferenceDefaultDateValue }

    suspend fun saveLoginSession(userToken:String, userName:String, userContact: String, userEmail: String) {
        dataStore.edit { preferences ->
            preferences[token] = userToken
            preferences[username] = userName
            preferences[contact] = userContact
            preferences[email] = userEmail
            preferences[lastLogin] = Helper.getCurrentDateString()
        }
    }

    suspend fun clearLoginSession() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: SettingPreferences? = null
        fun getInstance(dataStore: DataStore<Preferences>): SettingPreferences {
            return INSTANCE ?: synchronized(this) {
                val instance = SettingPreferences(dataStore)
                INSTANCE = instance
                instance
            }
        }
    }
}