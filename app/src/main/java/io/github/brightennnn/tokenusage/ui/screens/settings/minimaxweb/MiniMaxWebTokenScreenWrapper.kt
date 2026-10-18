package io.github.brightennnn.tokenusage.ui.screens.settings.minimaxweb

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun MiniMaxWebTokenScreenWrapper(
    onNavigateBack: () -> Unit,
    viewModel: MiniMaxWebTokenViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    MiniMaxWebTokenScreen(
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
