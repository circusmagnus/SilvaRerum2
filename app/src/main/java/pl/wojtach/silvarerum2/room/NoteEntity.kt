package pl.wojtach.silvarerum2.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import pl.wojtach.silvarerum2.NoteId
import pl.wojtach.silvarerum2.NoteSnapshot
import pl.wojtach.silvarerum2.Timestamp
import pl.wojtach.silvarerum2.utils.HasStableId

@Entity(tableName = NoteEntity.TABLE_NAME)
data class NoteEntity(
    @PrimaryKey override val id: String,
    val timestamp: Long,
    val content: String,
    @ColumnInfo(defaultValue = LAST_MODIFIED_DEFAULT.toString()) val lastModified: Long = timestamp
) : HasStableId {
    fun asNote(): NoteSnapshot =
        NoteSnapshot(NoteId(id), created = Timestamp(timestamp), content, lastModified = Timestamp(lastModified))

    companion object {
        const val TABLE_NAME = "Notes"
        const val LAST_MODIFIED_DEFAULT = -1L
    }
}

fun NoteSnapshot.toRoomEntity(): NoteEntity =
    NoteEntity(noteId.value, timestamp = created.value, content, lastModified = lastModified.value)