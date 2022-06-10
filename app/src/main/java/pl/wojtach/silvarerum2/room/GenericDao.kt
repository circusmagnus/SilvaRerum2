package pl.wojtach.silvarerum2.room

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import pl.wojtach.silvarerum2.utils.HasStableId

interface GenericDao<T> {

    @Insert
    suspend fun insert(note: NoteEntity): Long

    @Update
    suspend fun update(note: NoteEntity): Int

    @Delete
    suspend fun delete(note: NoteEntity)

    @Transaction
    suspend fun upsert(note: NoteEntity) {
        val updated = update(note)
        if(updated == 0) insert(note)
    }
}