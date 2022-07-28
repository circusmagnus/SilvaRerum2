package pl.wojtach.silvarerum2.room

import androidx.room.DeleteColumn
import androidx.room.migration.AutoMigrationSpec

@DeleteColumn(tableName = NoteEntity.TABLE_NAME, columnName = "priority")
class RemovePriorityMigration: AutoMigrationSpec