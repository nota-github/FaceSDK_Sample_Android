package com.nota.hyundai_lobby.http

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface HyundaiAPI {

    @GET("ht/api/intruder_img")
    fun getIntruderImage(
        @Query("img_key") imgKey : String
    ): Call<ResponseBody>

    @Headers("accept: application/json", "content-type: application/json")
    @POST("ht/api/get_members")
    fun getMembers() : Call<ResponseBody>

    @Headers("accept: application/json", "content-type: application/json")
    @POST("ht/api/get_intruder")
    fun getIntruder(@Body params: String) : Call<ResponseBody>

    @Headers("accept: application/json", "content-type: application/json")
    @POST("ht/api/get_history")
    fun getHistory(@Body params: String) : Call<ResponseBody>

    @Headers("accept: application/json", "content-type: application/json")
    @POST("ht/api/auth")
    fun getAuth(@Body params: String) : Call<ResponseBody>

    @Headers("accept: application/json", "content-type: application/json")
    @POST("ht/api/reg_member")
    fun regMember(@Body params: String) : Call<ResponseBody>

    @Headers("accept: application/json", "content-type: application/json")
    @POST("ht/api/reg_visitor")
    fun regVisitor(@Body params: String) : Call<ResponseBody>

    @Headers("accept: application/json", "content-type: application/json")
    @POST("ht/api/test")
    fun test(@Body params: String) : Call<ResponseBody>

    @Headers("accept: application/json", "content-type: application/json")
    @POST("ht/api/del_member")
    fun delMember(@Body params: String) : Call<ResponseBody>

}