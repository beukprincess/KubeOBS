package com.example.kubeobs

sealed interface KubernetesPodsList {
    data class Pod(val all: String){
    }
}

