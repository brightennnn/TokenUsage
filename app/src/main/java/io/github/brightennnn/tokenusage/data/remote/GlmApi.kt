package io.github.brightennnn.tokenusage.data.remote

import io.github.brightennnn.tokenusage.data.remote.GlmResetQuotaResponse
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

    /**
     * 用量重置额度（重置券）。2026-10-16 逆向官网 XHR 抓到的真实端点。
     *
     * ⚠️ 属 /api/biz/ 族（biz 后缀非通配）：**必须带 Referer**，否则 401 "token expired or incorrect"。
     * GlmApiRetrofitModule 里给本调用单独挂 Referer header。
     */
    @GET("api/biz/customer-package-reset/list")
    suspend fun getResetQuota(
        @Header("Authorization") authorization: String,
        @Header("Referer") referer: String = "https://open.bigmodel.cn/coding-plan/personal/usage",
        @Header("Accept-Language") acceptLanguage: String = "en-US,en",
        @retrofit2.http.Query("targetType") targetType: String = "PERSONAL"
    ): GlmResetQuotaResponse
}
