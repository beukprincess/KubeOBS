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

data class PodsInfoResponse(
    val status: String,
    val totalPods: Int,
    val pods: List<PodHealth>
)
data class PodHealth(
    val name: String,
    val namespace: String,
    val status: String,
    val restarts: Int,
    val ageSeconds: Int
)

