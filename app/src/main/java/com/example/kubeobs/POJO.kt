package com.example.kubeobs

import com.google.gson.annotations.SerializedName

data class NodesResponse(
    val status: String,
    val nodes: List<String>
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
    @SerializedName("age_seconds")
    val ageSeconds: Int
)
data class MetricsResponse(
    val status: String,
    val metrics: Metrics
)
data class Metrics(
    @SerializedName("cpu_percentage")
    val cpuPercentage: Float,
    val ram: Ram,
    val disk: Disk
)
data class Ram(
    @SerializedName("total_gb")
    val totalGB: Float,
    @SerializedName("used_gb")
    val usedGB: Float,
    val percentage: Float
)
data class Disk(
    @SerializedName("total_gb")
    val totalGB: Float,
    @SerializedName("free_gb")
    val freeGB: Float,
    val percentage: Float
)

























