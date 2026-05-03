package com.example.kubeobs

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers

interface RetrofitInterface {
    @Headers("X-Auth-Token: 509d90svckmxb098283905535j456jimbvi")
    @GET("/nodes")
    suspend fun getNodesInfo(): Response<NodesResponse>
    @Headers("X-Auth-Token: 509d90svckmxb098283905535j456jimbvi")
    @GET("/nodes")
    suspend fun getPodsInfo(): Response<PodsResponse>
}