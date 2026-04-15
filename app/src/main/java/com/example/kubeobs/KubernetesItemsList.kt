package com.example.kubeobs

sealed interface KubernetesItemsList {
    data class Cluster(val name: String){
        data class NamespaceHeader(val name: String): KubernetesItemsList
        data class Node(val name: String, val cpuUsage: Float, val memoryUsage: Float): KubernetesItemsList
        data class Pod(val name: String, val status: String, val restartCount: Int): KubernetesItemsList
    }
}

