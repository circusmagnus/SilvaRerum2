package pl.wojtach.silvarerum2

import pl.wojtach.silvarerum2.utils.HasStableId

data class NoteSnapshot(
    val noteId: NoteId,
    val created: Timestamp,
    val content: String,
    val lastModified: Timestamp = created,
    val reversedShowIndex: Int
): HasStableId {
    override val id: String
        get() = noteId.value
}

@JvmInline
value class NoteId(val value: String)

@JvmInline
value class Timestamp(val value: Long): Comparable<Timestamp> {
    override fun compareTo(other: Timestamp): Int = this.value.compareTo(other.value)
}