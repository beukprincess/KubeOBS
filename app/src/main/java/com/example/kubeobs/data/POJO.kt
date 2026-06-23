package com.example.kubeobs.data

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
    val source: String,
    @SerializedName("nodes_count")
    val nodesCount: Int,
    @SerializedName("cluster_metrics")
    val clusterMetrics: List<Metrics>
)
data class Metrics(
    @SerializedName("node_name")
    val nodeName: String,
    @SerializedName("cpu_usage")
    val cpuUsage: String,
    @SerializedName("memory_used_mb")
    val memoryUsedMb: Double,
)
data class RegisterRequest(
    val email: String,
    val password: String
)
data class RegisterResponse(
    val id: Int,
    val email: String,
    @SerializedName("is_active")
    val isActive: Boolean
)
data class UserInfo(
    val id: Int,
    val email: String
)
data class LoginRequest(
    val email: String,
    val password: String
)
data class LoginResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    val user: UserInfo
)
data class AddClusterRequest(
    val name: String,
    @SerializedName("endpoint_url")
    val endpointUrl: String,
    @SerializedName("cluster_token")
    val clusterToken: String
)
data class ClusterResponse(
    val id: Int,
    val name: String,
    @SerializedName("endpoint_url")
    val endpointUrl: String,
    @SerializedName("user_id")
    val userId: Int
)

























