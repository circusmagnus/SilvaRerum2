package pl.wojtach.silvarerum2.room

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [NoteEntity::class],
    version = 2,
    autoMigrations = [AutoMigration(from = 1, to = 2)]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notesDao(): NotesDao

    companion object {
        const val DB_NAME = "App_database"
    }
}