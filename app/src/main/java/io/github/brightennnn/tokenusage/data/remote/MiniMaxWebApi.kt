package io.github.brightennnn.tokenusage.data.remote

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

/**
 * MiniMax 网页控制台 BFF 接口（https://www.minimax.cn）。
 *
 * 鉴权：Cookie header 里放 `_token=<JWT>`（网页登录后的登录态 JWT）。
 * - 只用 `_token` 一个字段就够，`X-Group-Id` 可有可无（2026-10-17 实测）。
 * - WAF 不拦 OkHttp/curl。
 * - 失败返回 `{"base_resp":{"status_code":1004,"status_msg":"not login"}}` → HTTP 仍是 200，
 *   **判定成功看 status_code，不看 HTTP 码**。
 *
 * 用途（2026-10-18 用户拍板：`_token` 不再限定「只查重置券」）：
 * - 用量重置额度（重置券）
 * - 积分余额
 * - 用量统计（当日/近7日/近30日/全部）
 *
 * ⚠️ 但 5小时/本周 token 额度仍然走 MiniMax API Key（sk-cp-xxx）的
 *    /v1/token_plan/remains —— 两者互不混用，不要试图用 _token 换额度。
 */
interface MiniMaxWebApi {

    /**
     * 用量重置额度（重置券）列表。
     * 真实接口：GET https://www.minimax.cn/backend/account/reset_card/list?page=1&size=20
     */
    @GET("backend/account/reset_card/list")
    suspend fun getResetCards(
        @Header("Cookie") cookie: String,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): MiniMaxResetCardResponse

    /**
     * Token Plan 积分余额 + 每笔积分包的过期时间。
     * 真实接口：GET https://www.minimax.cn/backend/account/token_plan_credit
     *
     * ⚠️ 不要用 `/backend/account/usage` 查积分 —— 那是开放平台按量付费的
     *    media 积分，Token Plan 用户恒为 0。
     */
    @GET("backend/account/token_plan_credit")
    suspend fun getCredit(
        @Header("Cookie") cookie: String
    ): MiniMaxCreditResponse

    /**
     * 用量概览（当日 / 近7日 / 近30日）。
     * 真实接口：GET https://www.minimax.cn/backend/account/token_plan/usage_overview?period=7d
     *
     * @param period **只能传 day / 7d / 30d**。传其它值不报错，静默按 7d 返回。
     */
    @GET("backend/account/token_plan/usage_overview")
    suspend fun getUsageOverview(
        @Header("Cookie") cookie: String,
        @Query("period") period: String
    ): MiniMaxUsageOverviewResponse

    /**
     * 长期统计（「全部」周期）。
     * 真实接口：GET https://www.minimax.cn/backend/account/token_plan/usage_summary
     */
    @GET("backend/account/token_plan/usage_summary")
    suspend fun getUsageSummary(
        @Header("Cookie") cookie: String
    ): MiniMaxUsageSummaryResponse

    /**
     * 小时明细 —— 补当日模型拆分（usage_overview?period=day 的 trend 不含 models）。
     * 真实接口：
     *   GET .../backend/account/token_plan/usage_hourly_detail?start_time=2026-10-18&end_time=2026-10-18
     *
     * @param startTime 纯日期字符串 "yyyy-MM-dd"（传时间戳会解析失败）
     * @param endTime   同上；与 startTime 相同表示只查当天
     */
    @GET("backend/account/token_plan/usage_hourly_detail")
    suspend fun getHourlyDetail(
        @Header("Cookie") cookie: String,
        @Query("start_time") startTime: String,
        @Query("end_time") endTime: String
    ): MiniMaxHourlyDetailResponse
}
