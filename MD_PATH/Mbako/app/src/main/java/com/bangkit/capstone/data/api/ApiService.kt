package com.bangkit.capstone.data.api

import com.bangkit.capstone.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @POST("login")
    @FormUrlEncoded
    fun login(
        @Field("username") email: String,
        @Field("password") password: String
    ): Call<ResponseLogin>

    @POST("register")
    @FormUrlEncoded
    fun register(
        @Field("username") username: String,
        @Field("contact") contact: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<ResponseRegister>

    @POST("forgot-password")
    @FormUrlEncoded
    fun forgotPassword(
        @Field("email") email: String
    ): Call<ResponseForgotPass>

    @POST("reset-password")
    @FormUrlEncoded
    fun resetPassword(
        @Field("username") username: String,
        @Field("token") token : String,
        @Field("new_password") new_password : String
    ): Call<ResponseResetPassword>

    @Multipart
    @POST("api/items")
    fun sellItems(
        @Header("authorization") token: String,
        @Part image: MultipartBody.Part,
        @Part ("pname") pname: RequestBody,
        @Part ("price") price: RequestBody,
        @Part ("quantity") quantity: RequestBody
    ): Call<ResponseSell>

    @GET("api/items")
    suspend fun getItemList(
        @Header("authorization") token: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): DataItemList

    @POST("api/items/buy")
    @FormUrlEncoded
    fun buyItems(
       @Header("authorization") token: String,
       @Field("item_id") item_id: String,
       @Field("quantity") quantity: Int
    ): Call<ResponseBuy>
}
