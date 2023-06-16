package com.bangkit.capstone.ui.home.sell

import android.content.Context
import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bangkit.capstone.Constanta
import com.bangkit.capstone.R
import com.bangkit.capstone.data.api.ApiConfig
import com.bangkit.capstone.data.model.ResponseSell
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class SellViewModel: ViewModel() {

    var loading = MutableLiveData(View.GONE)
    var error = MutableLiveData("")
    var isSuccessSellItems= MutableLiveData(false)

    fun sellItems(
        context: Context,
        token: String,
        image: File,
        pname: String,
        price: Int,
        quantity: Int,
    ){
        loading.postValue(View.VISIBLE)
        "${image.length() / 1024 / 1024} MB"
        val itemName = pname.toRequestBody("text/plain".toMediaType())
        val itemPrice = price.toString().toRequestBody("text/plain".toMediaType())
        val itemQuantity = quantity.toString().toRequestBody("text/plain".toMediaType())

        val requestImageFile = image.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val imageMultiPart: MultipartBody.Part = MultipartBody.Part.createFormData(
            "image",
            image.name,
            requestImageFile
        )

        val client = ApiConfig.getApiService()
            .sellItems(
                token,
                imageMultiPart,
                itemName,
                itemPrice,
                itemQuantity
            )

        client.enqueue(object: Callback<ResponseSell>{
            override fun onResponse(call: Call<ResponseSell>, response: Response<ResponseSell>) {
                when (response.code()){
//                    422 -> error.postValue(context.getString(R.string.API_error_header_token))
                    200 -> isSuccessSellItems.postValue(true)
                    else -> {
                        val errorMessage = "Error ${response.code()} : ${response.message()}"
                        error.postValue(errorMessage)
                        Log.e(TAG, errorMessage)
                    }
                }
                loading.postValue(View.GONE)
            }

            override fun onFailure(call: Call<ResponseSell>, t: Throwable) {
                loading.postValue(View.GONE)
                val errorMessage = "${context.getString(R.string.API_error_send_payload)} : ${t.message}"
                error.postValue(errorMessage)
                Log.e(TAG, errorMessage)
            }

        })
    }

    companion object{
        private val TAG = "SellViewModel"
    }
}