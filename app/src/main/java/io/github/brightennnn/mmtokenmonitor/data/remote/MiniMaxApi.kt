package io.github.brightennnn.mmtokenmonitor.data.remote

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface MiniMaxApi {
    @GET("v1/api/openplatform/coding_plan/remains")
    suspend fun getTokenPlan(
        @Header("Authorization") authorization: String,
        @Query("GroupId") groupId: String,
        @Header("Content-Type") contentType: String = "application/json"
    ): TokenPlanResponse
}
