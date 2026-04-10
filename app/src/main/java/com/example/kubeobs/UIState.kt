package com.example.kubeobs

sealed interface UIState{
    object Loading: UIState
    data class Success(val data: String): UIState
    data class Error(val e: String): UIState
}
