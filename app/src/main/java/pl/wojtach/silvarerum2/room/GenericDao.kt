package pl.wojtach.silvarerum2.room

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Transaction
import androidx.room.Update

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