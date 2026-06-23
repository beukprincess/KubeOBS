package com.example.kubeobs.data

import com.example.kubeobs.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val base_url = BuildConfig.base_url
private val retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(base_url)
    .build()

object RetrofitAPI {
    val instance : RetrofitInterface by lazy {
        retrofit.create(RetrofitInterface::class.java)
    }
}