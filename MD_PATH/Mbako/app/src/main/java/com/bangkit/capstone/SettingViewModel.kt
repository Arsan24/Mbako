package com.bangkit.capstone

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class SettingViewModel(private val pref: SettingPreferences): ViewModel() {
    fun getUserPreferences(property:String): LiveData<String> {
        return when(property){
            Constanta.UserPreferences.UserName.name -> pref.getUserName().asLiveData()
            Constanta.UserPreferences.UserToken.name -> pref.getUserToken().asLiveData()
            Constanta.UserPreferences.UserContact.name -> pref.getUserContact().asLiveData()
            Constanta.UserPreferences.UserEmail.name -> pref.getUserEmail().asLiveData()
            Constanta.UserPreferences.UserLastLogin.name -> pref.getUserLastLogin().asLiveData()
            else -> pref.getUserName().asLiveData()
        }
    }

    fun setUserPreferences(userToken:String, userName: String, userContact: String, userEmail: String) {
        viewModelScope.launch {
            pref.saveLoginSession(userToken,userName, userContact, userEmail)
        }
    }

    fun clearUserPreferences() {
        viewModelScope.launch {
            pref.clearLoginSession()
        }
    }
}