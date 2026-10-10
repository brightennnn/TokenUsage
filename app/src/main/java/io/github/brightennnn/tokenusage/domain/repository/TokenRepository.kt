package io.github.brightennnn.tokenusage.domain.repository

import io.github.brightennnn.tokenusage.domain.model.ApiKeyProfile
import io.github.brightennnn.tokenusage.domain.model.ModelQuota
import kotlinx.coroutines.flow.Flow

interface TokenRepository {
    /** 当前激活的 API Key（Flow） */
    val activeApiKey: Flow<String?>

    /** 当前激活 Key 对应的 GroupId（Flow） */
    val activeGroupId: Flow<String?>

    /** 所有 API Key Profile 列表 */
    val apiKeyProfiles: Flow<List<ApiKeyProfile>>

    /** 保存 API Key（追加到列表） */
    suspend fun saveApiKey(key: String, name: String, groupId: String = "")

    /** 更新已有 Profile */
    suspend fun updateApiKeyProfile(profile: ApiKeyProfile)

    /** 删除 API Key Profile */
    suspend fun deleteApiKeyProfile(id: String)

    /** 设置激活的 Key */
    suspend fun setActiveApiKey(id: String)

    /** 清除所有 Key */
    suspend fun clearAllApiKeys()

    suspend fun fetchQuotas(apiKey: String, groupId: String = ""): Result<List<ModelQuota>>
}
