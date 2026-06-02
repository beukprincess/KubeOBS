package com.example.kubeobs

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers

interface RetrofitInterface {
    @Headers("X-Auth-Token: uVuxYa/e8+Ox4rK0pgYY7sCR/ArdAp8eeBIVpxGSlKE=")
    @GET("/nodes")
    suspend fun getNodesInfo(): Response<NodesResponse>
    @Headers("X-Auth-Token: uVuxYa/e8+Ox4rK0pgYY7sCR/ArdAp8eeBIVpxGSlKE=")
    @GET("/pods")
    suspend fun getPodsInfo(): Response<PodsResponse>
}