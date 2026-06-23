package com.example.kubeobs.data

import com.google.gson.annotations.SerializedName

data class WsMetricsMessage(
    @SerializedName("cluster_id")
    val clusterId: Int,
    @SerializedName("cluster_name")
    val clusterName: String,
    @SerializedName("total_pods")
    val totalPods: Int,
    @SerializedName("nodes_usage")
    val nodesUsage: List<NodeUsage> = emptyList(),
    val alerts: List<WsAlert> = emptyList(),
    val pods: List<WsPodMetric> = emptyList()
)

data class NodeUsage(
    @SerializedName("node_name")
    val nodeName: String,
    @SerializedName("cpu_usage")
    val cpuUsage: String,
    @SerializedName("memory_used_mb")
    val memoryUsedMb: Double
)

data class WsAlert(
    val level: String,
    val message: String
)

data class WsPodMetric(
    val name: String,
    val namespace: String,
    val status: String?,
    val restarts: Int,
    @SerializedName("memory_mb")
    val memoryMb: Double,
    @SerializedName("cpu_raw")
    val cpuRaw: String
)

data class WsErrorMessage(
    val error: String,
    val type: String? = null
)