package io.github.brightennnn.mmtokenmonitor.data.remote

import retrofit2.http.GET
import retrofit2.http.Header

/**
 * GLM Coding Plan 用量接口。
 *
 * 鉴权: Authorization header 直接放 bigmodel_token_production JWT（不带 Bearer 前缀）。
 *
 * 关键端点（本次只用到 quota/limit；model-usage/tool-usage 暂不展示）：
 *   GET /api/monitor/usage/model-usage
 *   GET /api/monitor/usage/tool-usage
 *   GET /api/monitor/usage/quota/limit
 */
interface GlmApi {
    @GET("api/monitor/usage/quota/limit")
    suspend fun getQuotaLimit(
        @Header("Authorization") authorization: String,
        @Header("Accept-Language") acceptLanguage: String = "en-US,en"
    ): GlmQuotaResponse
}
