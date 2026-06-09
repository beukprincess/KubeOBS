package com.example.kubeobs.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface RetrofitInterface {
    @Headers("X-Auth-Token: uVuxYa/e8+Ox4rK0pgYY7sCR/ArdAp8eeBIVpxGSlKE=")
    @GET("/nodes")
    suspend fun getNodes(): Response<NodesResponse>
    @Headers("X-Auth-Token: uVuxYa/e8+Ox4rK0pgYY7sCR/ArdAp8eeBIVpxGSlKE=")
    @GET("/pods")
    suspend fun getPods(): Response<PodsResponse>
    @Headers("X-Auth-Token: uVuxYa/e8+Ox4rK0pgYY7sCR/ArdAp8eeBIVpxGSlKE=")
    @GET("/pods/health")
    suspend fun getPodsInfo(): Response<PodsInfoResponse>
    @Headers("X-Auth-Token: uVuxYa/e8+Ox4rK0pgYY7sCR/ArdAp8eeBIVpxGSlKE=")
    @GET("/system/metrics")
    suspend fun getMetrics(): Response<MetricsResponse>
    @POST("/auth/register")
    suspend fun registerUser(
        @Body request: RegisterRequest
    ): Response<UserResponse>
}