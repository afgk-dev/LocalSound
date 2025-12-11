package dev.afgk.localsound.ui.sync

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import dev.afgk.localsound.data.synchronize.DatabaseSyncWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface SyncTracksUiState {
    object Init : SyncTracksUiState
    object Syncing : SyncTracksUiState
    object Synced : SyncTracksUiState
    object Failed : SyncTracksUiState
}

class SyncTracksViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<SyncTracksUiState>(SyncTracksUiState.Init)
    val uiState = _uiState.asStateFlow()

    fun sync(context: Context) {
        viewModelScope.launch {
            val workManager = WorkManager.getInstance(context)

            val workRequest = OneTimeWorkRequestBuilder<DatabaseSyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                        .setRequiresCharging(false)
                        .build()
                )
                .build()

            workManager.enqueueUniqueWork(
                DatabaseSyncWorker::class.java.simpleName,
                ExistingWorkPolicy.KEEP,
                workRequest
            )

            workManager.getWorkInfoByIdFlow(workRequest.id)
                .collect { info ->
                    if (info?.state != null) {
                        when (info.state) {
                            WorkInfo.State.SUCCEEDED -> SyncTracksUiState.Synced
                            WorkInfo.State.FAILED -> SyncTracksUiState.Failed
                            WorkInfo.State.RUNNING -> SyncTracksUiState.Syncing
                            else -> null
                        }.let { state ->
                            if (state != null) _uiState.update { state }
                        }
                    }
                }
        }
    }
}