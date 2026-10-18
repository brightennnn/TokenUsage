package io.github.brightennnn.tokenusage.ui.screens.settings.apikey

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.brightennnn.tokenusage.ui.screens.settings.SettingsViewModel
import io.github.brightennnn.tokenusage.ui.screens.settings.minimaxweb.MiniMaxWebTokenViewModel

/**
 * MiniMax账号管理页 Wrapper。
 *
 * 两个 ViewModel 各司其职：
 *   - SettingsViewModel          → API Key 多档位（必填）
 *   - MiniMaxWebTokenViewModel   → 网页 Token 单凭证（选填，查重置额度）
 */
@Composable
fun ApiKeyManagementScreenWrapper(
    onNavigateBack: () -> Unit,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    webTokenViewModel: MiniMaxWebTokenViewModel = hiltViewModel(),
) {
    val profiles by settingsViewModel.apiKeyProfiles.collectAsState(initial = emptyList())
    val activeProfileId by settingsViewModel.activeApiKeyId.collectAsState(initial = null)
    val webTokenState by webTokenViewModel.uiState.collectAsStateWithLifecycle()

    ApiKeyManagementScreen(
        profiles = profiles,
        activeProfileId = activeProfileId,
        webTokenState = webTokenState,
        onNavigateBack = onNavigateBack,
        onAddKey = { name, key, groupId ->
            settingsViewModel.addApiKey(name, key, groupId)
        },
        onActivate = { id ->
            settingsViewModel.activateApiKey(id)
        },
        onDelete = { id ->
            settingsViewModel.deleteApiKey(id)
        },
        onUpdateProfile = { profile ->
            settingsViewModel.updateApiKeyProfile(profile)
        },
        onWebTokenStartEditing = webTokenViewModel::startEditing,
        onWebTokenCancelEditing = webTokenViewModel::cancelEditing,
        onWebTokenDraftChange = webTokenViewModel::updateDraft,
        onWebTokenToggleShow = webTokenViewModel::toggleShowToken,
        onWebTokenSave = webTokenViewModel::save,
        onWebTokenSaveAndTest = webTokenViewModel::saveAndTest,
        onWebTokenDelete = webTokenViewModel::deleteToken,
    )
}
