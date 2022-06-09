package pl.wojtach.silvarerum2.room

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
    val content: String
): HasStableId {
    fun asNote(): NoteSnapshot = NoteSnapshot(NoteId(id), Timestamp(timestamp), content)

    companion object {
        const val TABLE_NAME = "Notes"
    }
}

fun NoteSnapshot.toRoomEntity(): NoteEntity = NoteEntity(noteId.value, created.value, content)