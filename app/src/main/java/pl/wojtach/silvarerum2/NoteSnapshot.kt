package pl.wojtach.silvarerum2

import pl.wojtach.silvarerum2.utils.HasStableId

data class NoteSnapshot(
    val noteId: NoteId,
    val created: Timestamp,
    val content: String
): HasStableId {
    override val id: String
        get() = noteId.value
}

@JvmInline
value class NoteId(val value: String)

@JvmInline
value class Timestamp(val value: Long)