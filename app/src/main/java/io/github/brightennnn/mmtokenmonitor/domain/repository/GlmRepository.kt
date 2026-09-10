package io.github.brightennnn.mmtokenmonitor.domain.repository

import io.github.brightennnn.mmtokenmonitor.domain.model.GlmQuota
import kotlinx.coroutines.flow.Flow

/**
 * GLM Coding Plan 仓库接口。
 *
 * 与 TokenRepository（MiniMax）完全独立 — 凭证存储、接口域、鉴权方式都不同。
 * 仅展示 5小时窗 + 周窗（用户拍板：工具月窗完全不显示）。
 */
interface GlmRepository {
    /** 当前保存的 JWT token（Flow）。空表示未配置。 */
    val glmToken: Flow<String?>

    /** 保存/替换 JWT。保存即设为唯一凭证（只存一条）。 */
    suspend fun saveToken(token: String)

    /** 删除 JWT。 */
    suspend fun clearToken()

    /**
     * 拉取用量并转换为展示模型。
     * @param token  JWT（默认读 DataStore 中保存的）
     * @return 成功时只包含 5小时窗 + 周窗（按 usedPercent 倒序）
     */
    suspend fun fetchQuotas(token: String = ""): Result<List<GlmQuota>>
}
