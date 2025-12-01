package dev.afgk.localsound.data.queue

class QueueRepository(private val queueDao: QueueDao) {
    fun getQueue() = queueDao.getQueueWithTracks()

    suspend fun addToQueue(trackId: Long){
        val lastPosition = queueDao.getLastPosition() ?: 0
        val newPosition = lastPosition + 1

        val queueItem = QueueTrackEntity(
            id = 0,
            trackId = trackId,
            position = newPosition,
            isCustomQueue = true,
            isCurrent = false
        )
        queueDao.insert(queueItem)
    }

    suspend fun removeFromQueue(queueItem: QueueTrackEntity){
        queueDao.delete(queueItem)
    }
}