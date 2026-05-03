package com.example.kubeobs

sealed interface UIState{
    object Loading: UIState
    data class Success(val data: NodesResponse?): UIState
    data class Error(val e: String): UIState
}
