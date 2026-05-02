package com.example.kubeobs

data class User(
    val username: String,
    val password: String
)

data class Users(
    val users: List<User>
)
