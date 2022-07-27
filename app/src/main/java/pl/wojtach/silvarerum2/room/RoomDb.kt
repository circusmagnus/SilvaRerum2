package pl.wojtach.silvarerum2.room

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [NoteEntity::class],
    version = 3,
    autoMigrations = [AutoMigration(from = 1, to = 2), AutoMigration(from = 2, to = 3)]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notesDao(): NotesDao

    companion object {
        const val DB_NAME = "App_database"
    }
}