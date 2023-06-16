package com.bangkit.capstone.ui.resetpassword

import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bangkit.capstone.Constanta
import com.bangkit.capstone.data.api.ApiConfig
import com.bangkit.capstone.data.model.ResponseResetPassword
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ResetPasswordViewModel: ViewModel() {
    var loading = MutableLiveData(View.GONE)
    val error = MutableLiveData("")

    val resetResult = MutableLiveData<ResponseResetPassword>()

    fun resetPassword(username:String, token:String, new_password:String){
        loading.postValue(View.VISIBLE)
        val client = ApiConfig.getApiService().resetPassword(username, token, new_password)
        client.enqueue(object: Callback<ResponseResetPassword> {
            override fun onResponse(
                call: Call<ResponseResetPassword>,
                response: Response<ResponseResetPassword>,
            ) {
                if(response.isSuccessful){
                    resetResult.postValue(response.body())
                }else{
                    error.postValue("ERROR RESET PASS: ${response.message()}")
                }
                loading.postValue(View.GONE)

            }

            override fun onFailure(call: Call<ResponseResetPassword>, t: Throwable) {
                loading.postValue(View.GONE)
                Log.e(Constanta.TAG_RESET_PASS, "onFailure Call: ${t.message}")
                error.postValue(t.message)
            }
        })
    }

}