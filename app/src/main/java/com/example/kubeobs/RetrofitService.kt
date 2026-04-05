package com.example.kubeobs

import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

private const val BASE_URL =
    "https://android-kotlin-fun-mars-server.appspot.com"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(ScalarsConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

object RetrofitAPI {
    val retrofitService : RetrofitInterface by lazy {
        retrofit.create(RetrofitInterface::class.java)
    }
}