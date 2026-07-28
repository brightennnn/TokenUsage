package io.github.brightennnn.mmtokenmonitor.data.remote

import com.google.gson.annotations.SerializedName

data class TokenPlanResponse(
    @SerializedName("model_remains")
    val modelRemains: List<ModelRemainDto>,
    @SerializedName("base_resp")
    val baseResp: BaseRespDto?
)

data class ModelRemainDto(
    @SerializedName("model_name")
    val modelName: String,
    @SerializedName("current_interval_total_count")
    val currentIntervalTotalCount: Int = 0,
    @SerializedName("current_interval_usage_count")
    val currentIntervalUsageCount: Int = 0,
    @SerializedName("current_weekly_total_count")
    val currentWeeklyTotalCount: Int = 0,
    @SerializedName("current_weekly_usage_count")
    val currentWeeklyUsageCount: Int = 0,
    @SerializedName("start_time")
    val startTime: Long,
    @SerializedName("end_time")
    val endTime: Long,
    @SerializedName("weekly_start_time")
    val weeklyStartTime: Long,
    @SerializedName("weekly_end_time")
    val weeklyEndTime: Long,
    // 新接口独有字段
    @SerializedName("remains_time")
    val remainsTime: Long = 0L,
    @SerializedName("weekly_remains_time")
    val weeklyRemainsTime: Long = 0L,
    @SerializedName("current_interval_status")
    val currentIntervalStatus: Int = 0,
    @SerializedName("current_weekly_status")
    val currentWeeklyStatus: Int = 0,
    @SerializedName("current_interval_remaining_percent")
    val currentIntervalRemainingPercent: Int = 0,
    @SerializedName("current_weekly_remaining_percent")
    val currentWeeklyRemainingPercent: Int = 0,
    @SerializedName("interval_boost_permille")
    val intervalBoostPermille: Int = 0,
    @SerializedName("weekly_boost_permille")
    val weeklyBoostPermille: Int = 0
)

data class BaseRespDto(
    @SerializedName("status_code")
    val statusCode: Int,
    @SerializedName("status_msg")
    val statusMsg: String
)
