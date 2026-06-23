package com.example.kubeobs.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface RetrofitInterface {
    @GET("/nodes")
    suspend fun getNodes(
        @Header("Authorization") token: String
    ): Response<NodesResponse>
    @GET("/pods")
    suspend fun getPods(
        @Header("Authorization") token: String
    ): Response<PodsResponse>
    @GET("/pods/health")
    suspend fun getPodsInfo(
        @Header("Authorization") token: String
    ): Response<PodsInfoResponse>
    @GET("/system/metrics")
    suspend fun getMetrics(
        @Header("Authorization") token: String?
    ): Response<MetricsResponse>
    @POST("auth/register")
    suspend fun registerUser(
        @Body body: RegisterRequest
    ): Response<RegisterResponse>
    @POST("auth/login")
    suspend fun loginUser(
        @Body body: LoginRequest
    ): Response<LoginResponse>
    @GET("clusters")
    suspend fun getClusters(
        @Header("Authorization") token: String
    ): Response<List<ClusterResponse>>
    @POST("clusters")
    suspend fun addCluster(
        @Header("Authorization") token: String,
        @Body body: AddClusterRequest
    ): Response<ClusterResponse>
    @GET("clusters/{id}/metrics")
    suspend fun getClusterMetrics(
        @Header("Authorization") token: String,
        @Path("id") clusterId: Int
    ): Response<MetricsResponse>
}