package com.bangkit.capstone.ui.home.detail

import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bangkit.capstone.Constanta
import com.bangkit.capstone.data.api.ApiConfig
import com.bangkit.capstone.data.model.ResponseBuy
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailViewModel: ViewModel() {
    var loading = MutableLiveData(View.GONE)
    val error = MutableLiveData("")

    val sellResult = MutableLiveData<ResponseBuy>()

    fun sellItems(token: String, item_id: String, quantity: Int){
        loading.postValue(View.VISIBLE)
        val client = ApiConfig.getApiService().buyItems(token, item_id, quantity)
        client.enqueue(object: Callback<ResponseBuy> {
            override fun onResponse(call: Call<ResponseBuy>, response: Response<ResponseBuy>) {
                if(response.isSuccessful){
                    sellResult.postValue(response.body())
                }else{
                    error.postValue("ERROR BUY: ${response.message()}")
                }
                loading.postValue(View.GONE)
            }

            override fun onFailure(call: Call<ResponseBuy>, t: Throwable) {
                loading.postValue(View.GONE)
                Log.e(Constanta.TAG_SELL_ITEM, "onFailure Call: ${t.message}")
                error.postValue(t.message)
            }

        })
    }
}