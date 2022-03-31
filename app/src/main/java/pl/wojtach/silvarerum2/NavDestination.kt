package pl.wojtach.silvarerum2

sealed class NavDestination {
    abstract val uri: String

    object NoteList : NavDestination() {

        val id: String = "notes/list"
        override val uri: String
            get() = id
    }

    data class ReadNote(val noteId: NoteId) : NavDestination() {
        override val uri = "notes/${noteId.value}/read"

        companion object {
            val id: String = "notes/{noteId}/read"
        }
    }

    data class EditNote(val noteId: NoteId) : NavDestination() {
        override val uri = "notes/${noteId.value}/edit"

        companion object {
            val id: String = "notes/{noteId}/edit"
        }
    }
}
