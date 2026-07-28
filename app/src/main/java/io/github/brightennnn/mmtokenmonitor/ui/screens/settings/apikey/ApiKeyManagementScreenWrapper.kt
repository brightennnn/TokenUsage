package io.github.brightennnn.mmtokenmonitor.ui.screens.settings.apikey

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.brightennnn.mmtokenmonitor.ui.screens.settings.SettingsViewModel

@Composable
fun ApiKeyManagementScreenWrapper(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val profiles by viewModel.apiKeyProfiles.collectAsState(initial = emptyList())
    val activeProfileId by viewModel.activeApiKeyId.collectAsState(initial = null)

    ApiKeyManagementScreen(
        profiles = profiles,
        activeProfileId = activeProfileId,
        onNavigateBack = onNavigateBack,
        onAddKey = { name, key, groupId ->
            viewModel.addApiKey(name, key, groupId)
        },
        onActivate = { id ->
            viewModel.activateApiKey(id)
        },
        onDelete = { id ->
            viewModel.deleteApiKey(id)
        },
        onUpdateProfile = { profile ->
            viewModel.updateApiKeyProfile(profile)
        }
    )
}
