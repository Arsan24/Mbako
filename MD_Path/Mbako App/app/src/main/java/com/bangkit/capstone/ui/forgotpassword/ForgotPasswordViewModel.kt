package com.bangkit.capstone.ui.forgotpassword

import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bangkit.capstone.Constanta
import com.bangkit.capstone.data.api.ApiConfig
import com.bangkit.capstone.data.model.ResponseForgotPass
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgotPasswordViewModel: ViewModel() {
    val tempEmail = MutableLiveData("")
    var loading = MutableLiveData(View.GONE)
    val error = MutableLiveData("")

    val forgotPassResult = MutableLiveData<ResponseForgotPass>()

    fun forgotPassword(email: String){
        tempEmail.postValue(email)
        loading.postValue(View.VISIBLE)
        val client = ApiConfig.getApiService().forgotPassword(email)
        client.enqueue(object: Callback<ResponseForgotPass>{
            override fun onResponse(
                call: Call<ResponseForgotPass>,
                response: Response<ResponseForgotPass>,
            ) {
                if(response.isSuccessful){
                    val responseData = response.body()
                    if (responseData != null && !responseData.error) {
                        responseData.let {
                            forgotPassResult.postValue(it)
                        }
                    } else {
                        error.postValue("ERROR FORGOT PASS: ${responseData?.message}")
                    }
                } else {
                    error.postValue("ERROR FORGOT PASS: ${response.message()}")
                }
                loading.postValue(View.GONE)

            }

            override fun onFailure(call: Call<ResponseForgotPass>, t: Throwable) {
                loading.postValue(View.GONE)
                Log.e(Constanta.TAG_FORGOT_PASS, "onFailure Call: ${t.message}")
                error.postValue(t.message)
            }

        })
    }

}