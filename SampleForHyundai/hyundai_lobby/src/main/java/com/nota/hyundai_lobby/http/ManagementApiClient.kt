package com.nota.hyundai_lobby.http

import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

object ManagementApiClient {

    const val baseUrl = "http://192.168.1.240"
    private var instance : Retrofit? = null

    fun getInstance() : Retrofit {
        if(instance == null){
            instance = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build()
        }
        return instance!!
    }
}
