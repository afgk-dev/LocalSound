package dev.afgk.localsound.data.releases

import androidx.room.Dao
import dev.afgk.localsound.data.core.BaseDao

@Dao
interface ReleaseDao: BaseDao<ReleaseEntity> {
    //Isn't better delete this too? Without getAll() there is no code
}