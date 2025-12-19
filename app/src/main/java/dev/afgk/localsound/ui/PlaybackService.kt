package dev.afgk.localsound.ui

import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dev.afgk.localsound.MyApplication
import dev.afgk.localsound.data.queue.QueueTrackEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // Lazy initialization para evitar acesso ao banco antes da hora se houver algum problema no di
    private val queueDao by lazy { MyApplication.appModule.database.queueDao() }

    override fun onGetSession(
        controllerInfo: MediaSession.ControllerInfo
    ): MediaSession? = mediaSession

    override fun onCreate() {
        super.onCreate()
        Log.d("PlaybackService", "onCreate iniciado")

        try {
            val player = ExoPlayer.Builder(this).build()
            player.addListener(object : Player.Listener {
                override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                    Log.d("PlaybackService", "onTimelineChanged: reason=$reason")
                    updateDatabaseQueue(player)
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    Log.d("PlaybackService", "onMediaItemTransition: ${mediaItem?.mediaId}")
                    mediaItem?.mediaId?.toLongOrNull()?.let { trackId ->
                        serviceScope.launch {
                            try {
                                withContext(Dispatchers.IO) {
                                    queueDao.updateCurrentTrack(trackId)
                                }
                                checkQueueSync(player)
                            } catch (e: Exception) {
                                Log.e("PlaybackService", "Erro ao atualizar track atual: ${e.message}")
                            }
                        }
                    }
                }
            })

            mediaSession = MediaSession.Builder(this, player).build()
            Log.d("PlaybackService", "MediaSession criada com sucesso")
        } catch (e: Exception) {
            Log.e("PlaybackService", "Erro no onCreate do PlaybackService: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun updateDatabaseQueue(player: Player) {
        val itemCount = player.mediaItemCount
        if (itemCount == 0) return

        val tracks = mutableListOf<QueueTrackEntity>()
        val currentIndex = player.currentMediaItemIndex
        
        for (i in 0 until itemCount) {
            val mediaItem = player.getMediaItemAt(i)
            mediaItem.mediaId.toLongOrNull()?.let { trackId ->
                tracks.add(
                    QueueTrackEntity(
                        id = 0,
                        position = i,
                        isCustomQueue = false,
                        isCurrent = i == currentIndex,
                        trackId = trackId
                    )
                )
            }
        }

        if (tracks.isNotEmpty()) {
            serviceScope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        queueDao.updateQueue(tracks)
                    }
                    checkQueueSync(player)
                } catch (e: Exception) {
                    Log.e("PlaybackService", "Erro ao atualizar fila no banco: ${e.message}")
                }
            }
        }
    }

    private suspend fun checkQueueSync(player: Player) {
        try {
            val playerIds = mutableListOf<Long>()
            val itemCount = player.mediaItemCount
            for (i in 0 until itemCount) {
                player.getMediaItemAt(i).mediaId.toLongOrNull()?.let { playerIds.add(it) }
            }

            val dbIds = withContext(Dispatchers.IO) {
                queueDao.getAllQueueTrackIds()
            }
            
            val isSynced = playerIds == dbIds

            Log.d("PlaybackService", "Contém ${playerIds.size} músicas - fila por ids: ${playerIds.joinToString(", ")}")
            Log.d("PlaybackService", "Fila no BD: ${dbIds.joinToString(", ")}")
            
            if (isSynced) {
                Log.d("PlaybackService", "✅ Sincronização: OK")
            } else {
                Log.w("PlaybackService", "⚠️ Sincronização: Player e BD estão temporariamente diferentes")
            }
        } catch (e: Exception) {
            Log.e("PlaybackService", "Erro ao conferir sincronismo: ${e.message}")
        }
    }

    override fun onDestroy() {
        Log.d("PlaybackService", "onDestroy iniciado")
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
