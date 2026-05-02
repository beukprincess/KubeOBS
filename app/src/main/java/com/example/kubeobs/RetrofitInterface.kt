package com.example.kubeobs

import retrofit2.http.GET
import retrofit2.http.Path

interface RetrofitInterface {
    @GET("/users")
    suspend fun getUsers(): Users
    @GET("/")
    suspend fun getAll(): String
    @GET("/users{id}")
    suspend fun getUserByID(@Path("id") id: Int): User
}