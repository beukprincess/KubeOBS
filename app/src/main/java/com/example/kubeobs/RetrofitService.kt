package com.example.kubeobs

import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

val base_url = BuildConfig.base_url
private val retrofit = Retrofit.Builder()
    .addConverterFactory(ScalarsConverterFactory.create())
    .baseUrl(base_url)
    .build()

object RetrofitAPI {
    val instance : RetrofitInterface by lazy {
        retrofit.create(RetrofitInterface::class.java)
    }
}