package pl.wojtach.silvarerum2.room

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Transaction
import androidx.room.Update

interface GenericDao<T> {

    @Insert
    suspend fun insert(entity: T): Long

    @Update
    suspend fun update(entity: T): Int

    @Delete
    suspend fun delete(entity: T)

    @Transaction
    suspend fun upsert(entity: T) {
        val updated = update(entity)
        if(updated == 0) insert(entity)
    }
}