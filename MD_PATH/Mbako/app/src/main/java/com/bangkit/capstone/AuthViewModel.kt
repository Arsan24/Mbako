package com.bangkit.capstone

import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bangkit.capstone.data.api.ApiConfig
import com.bangkit.capstone.data.model.ResponseLogin
import com.bangkit.capstone.data.model.ResponseRegister
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AuthViewModel: ViewModel() {

    val error = MutableLiveData("")
    val tempEmail = MutableLiveData("")
    val tempUsername = MutableLiveData("")
    val loginResult = MutableLiveData<ResponseLogin>()
    var loading = MutableLiveData(View.GONE)
    val registerResult = MutableLiveData<ResponseRegister>()

    fun login(username: String, password: String){
        tempUsername.postValue(username)
        loading.postValue(View.VISIBLE)
        val client = ApiConfig.getApiService().login(username, password)
        client.enqueue(object : Callback<ResponseLogin> {
            override fun onResponse(call: Call<ResponseLogin>, response: Response<ResponseLogin>) {
                if(response.isSuccessful){
                    loginResult.postValue(response.body())
                }else{
                    response.errorBody()?.let {
                        val errorResponse = JSONObject(it.string())
                        val errorMessages = errorResponse.getString("message")
                        error.postValue("LOGIN ERROR : $errorMessages")
                    }
                }
                loading.postValue(View.GONE)
            }

            override fun onFailure(call: Call<ResponseLogin>, t: Throwable) {
                loading.postValue(View.GONE)
                Log.e(TAGLogin,"onFailure Call: ${t.message}")
                error.postValue(t.message)
            }

        })
    }

    fun register(username: String, contact: String, email:String, password:String){
        loading.postValue(View.VISIBLE)
        val client = ApiConfig.getApiService().register(username, contact, email, password)
        client.enqueue(object : Callback<ResponseRegister> {
            override fun onResponse(call: Call<ResponseRegister>, response: Response<ResponseRegister>) {
                if(response.isSuccessful){
                    registerResult.postValue(response.body())
                }else{
                    response.errorBody()?.let {
                        val errorResponse = JSONObject(it.string())
                        val errorMessages = errorResponse.getString("message")
                        error.postValue("REGISTER ERROR : $errorMessages")
                    }
                }
                loading.postValue(View.GONE)
            }
            override fun onFailure(call: Call<ResponseRegister>, t: Throwable) {
                loading.postValue(View.GONE)
                error.postValue(t.message)
                Log.e(TAGRegister,"onFailure Call Connection: ${t.message}")
                t.printStackTrace()
            }

        })
    }


    companion object{
        private const val TAGLogin = "LoginViewModel"
        private const val TAGRegister = "RegisterViewModel"
    }
}