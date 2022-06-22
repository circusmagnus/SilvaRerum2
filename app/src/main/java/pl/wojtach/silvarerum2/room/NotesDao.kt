package pl.wojtach.silvarerum2.room

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotesDao: GenericDao<NoteEntity> {

    @Query("SELECT * FROM ${NoteEntity.TABLE_NAME}")
    fun getAll(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM ${NoteEntity.TABLE_NAME} WHERE id = :id")
    fun getById(id: String): Flow<NoteEntity>
}

