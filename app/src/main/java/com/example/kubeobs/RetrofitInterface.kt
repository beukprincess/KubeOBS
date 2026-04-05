package com.example.kubeobs

import retrofit2.http.GET

interface RetrofitInterface {
    @GET("/users")
    fun getUsers()
    @GET("/")
    fun getAll()
    @GET("/users")
    fun getUserByID(id: Int)
}