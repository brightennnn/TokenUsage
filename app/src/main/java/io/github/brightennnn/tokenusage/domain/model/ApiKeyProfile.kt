package io.github.brightennnn.tokenusage.domain.model

import java.util.UUID

/**
 * API Key 配置单元
 * @param id 唯一标识
 * @param name 显示名称（用户自定义）
 * @param key API Key 内容
 * @param groupId MiniMax GroupId（用于查询 Coding Plan 剩余量）
 * @param isActive 是否当前激活
 */
data class ApiKeyProfile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val key: String,
    val groupId: String = "",
    val isActive: Boolean = false
)
