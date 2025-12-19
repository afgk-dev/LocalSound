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
import dev.afgk.localsound.data.tracks.TrackEntity
import kotlinx.coroutines.*

class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val queueDao by lazy { MyApplication.appModule.database.queueDao() }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onCreate() {
        super.onCreate()
        try {
            val player = ExoPlayer.Builder(this).build()
            
            // Restaura a fila e inicia a reprodução automaticamente
            serviceScope.launch {
                try {
                    val orderedTracks: List<TrackEntity> = withContext(Dispatchers.IO) { 
                        queueDao.getTracksInQueueOrdered() 
                    }
                    
                    if (orderedTracks.isNotEmpty()) {
                        val currentTrackId: Long? = withContext(Dispatchers.IO) { 
                            queueDao.getCurrentTrackId() 
                        }
                        
                        val mediaItems = orderedTracks.map { track ->
                            MediaItem.Builder()
                                .setMediaId(track.id.toString())
                                .setUri(track.uri)
                                .build()
                        }
                        
                        val startIndex = if (currentTrackId != null) {
                            orderedTracks.indexOfFirst { it.id == currentTrackId }.coerceAtLeast(0)
                        } else {
                            0
                        }
                        
                        player.setMediaItems(mediaItems, startIndex, 0L)
                        player.prepare()
                        player.play()
                        
                        // Log solicitado: Lista encontrada
                        Log.d("PlaybackService", "Lista encontrada: Contém ${mediaItems.size} músicas - fila por ids: ${orderedTracks.map { it.id }.joinToString(", ")}")
                    } else {
                        Log.d("PlaybackService", "Nenhuma fila anterior encontrada.")
                    }
                } catch (e: Exception) {
                    Log.e("PlaybackService", "Erro ao restaurar fila: ${e.message}")
                }
            }

            player.addListener(object : Player.Listener {
                override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                    if (reason == Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED) {
                        updateDatabaseQueue(player)
                    }
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    mediaItem?.mediaId?.toLongOrNull()?.let { trackId ->
                        serviceScope.launch {
                            try {
                                withContext(Dispatchers.IO) { queueDao.updateCurrentTrack(trackId) }
                                checkQueueSync(player)
                            } catch (e: Exception) { Log.e("Queue", "Err: ${e.message}") }
                        }
                    }
                }
            })

            mediaSession = MediaSession.Builder(this, player).build()
        } catch (e: Exception) {
            Log.e("PlaybackService", "Crash prevent: ${e.message}")
        }
    }

    private fun updateDatabaseQueue(player: Player) {
        val count = player.mediaItemCount
        if (count == 0) return
        val tracks = (0 until count).mapNotNull { i ->
            player.getMediaItemAt(i).mediaId.toLongOrNull()?.let { id ->
                QueueTrackEntity(0, i, false, i == player.currentMediaItemIndex, id)
            }
        }
        serviceScope.launch {
            try {
                withContext(Dispatchers.IO) { queueDao.updateQueue(tracks) }
                checkQueueSync(player)
            } catch (e: Exception) { Log.e("Queue", "Save err: ${e.message}") }
        }
    }

    private suspend fun checkQueueSync(player: Player) {
        try {
            val playerIds = mutableListOf<Long>()
            for (i in 0 until player.mediaItemCount) {
                player.getMediaItemAt(i).mediaId.toLongOrNull()?.let { playerIds.add(it) }
            }
            
            // Log solicitado: Contém X músicas
            Log.d("PlaybackService", "Contém ${playerIds.size} músicas - fila por ids: ${playerIds.joinToString(", ")}")
            
            val dbIds = withContext(Dispatchers.IO) { queueDao.getAllQueueTrackIds() }
            val isSynced = playerIds == dbIds
            
            if (isSynced) Log.d("PlaybackService", "✅ Sincronização: OK")
            else Log.w("PlaybackService", "⚠️ Sincronização: Diferente")
        } catch (e: Exception) {}
    }

    override fun onDestroy() {
        serviceScope.cancel()
        mediaSession?.run {
            player.release()
            release()
        }
        super.onDestroy()
    }
}
