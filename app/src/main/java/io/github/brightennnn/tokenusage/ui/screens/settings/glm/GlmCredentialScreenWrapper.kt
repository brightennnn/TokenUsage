package io.github.brightennnn.tokenusage.ui.screens.settings.glm

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun GlmCredentialScreenWrapper(
    onNavigateBack: () -> Unit,
    viewModel: GlmCredentialViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    GlmCredentialScreen(
        state = state,
        onStartEditing = viewModel::startEditing,
        onCancelEditing = viewModel::cancelEditing,
        onDraftChange = viewModel::updateDraft,
        onToggleShow = viewModel::toggleShowToken,
        onSave = viewModel::save,
        onSaveAndTest = viewModel::saveAndTest,
        onDelete = viewModel::deleteToken,
        onNavigateBack = onNavigateBack,
    )
}
