package dev.afgk.localsound.data.queue

class QueueRepository(private val queueDao: QueueDao) {
    fun getQueue() = queueDao.getQueueWithTracksAndArtists()

    suspend fun addToQueue(trackId: Long){
        val lastPosition = queueDao.getLastPosition() ?: 0
        val newPosition = lastPosition + 1

        val queueItem = QueueTrackEntity(
            trackId = trackId,
            position = newPosition
        )
        queueDao.insert(queueItem)
    }

    suspend fun removeFromQueue(queueItem: QueueTrackEntity){
        queueDao.delete(queueItem)
    }
}