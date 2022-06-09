package pl.wojtach.silvarerum2.room

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import pl.wojtach.silvarerum2.utils.HasStableId

interface GenericDao<T> {

    @Insert
    suspend fun insert(note: NoteEntity)

    @Update
    suspend fun update(note: NoteEntity)

    @Delete
    suspend fun delete()
}