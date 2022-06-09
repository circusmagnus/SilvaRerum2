package pl.wojtach.silvarerum2.room

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import pl.wojtach.silvarerum2.NoteId
import pl.wojtach.silvarerum2.NoteSnapshot

@Dao
interface NotesDao: GenericDao<NoteEntity> {

    @Query("SELECT * FROM ${NoteEntity.TABLE_NAME}")
    fun getAll(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM ${NoteEntity.TABLE_NAME} WHERE id = :id")
    fun getById(id: String): Flow<NoteEntity>
}

