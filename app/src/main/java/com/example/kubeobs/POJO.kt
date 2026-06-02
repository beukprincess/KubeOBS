package com.example.kubeobs

data class NodesResponse(
    val status: String,
    val nodes: List<String>
)

data class NodeInfo(
    val ip: String,
    val namespace: String,
    val name: String
)

data class PodsResponse(
    val status: String,
    val pods: List<String>
)

data class PodInfo(
    val ip: String,
    val namespace: String,
    val name: String
)