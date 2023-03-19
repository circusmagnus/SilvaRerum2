package pl.wojtach.silvarerum2.manualdi

import kotlinx.coroutines.CoroutineScope
import pl.wojtach.silvarerum2.EditNoteModel
import pl.wojtach.silvarerum2.NoteSnapshot
import pl.wojtach.silvarerum2.ReadNoteModel
import pl.wojtach.silvarerum2.notelist.NoteListModel
import pl.wojtach.silvarerum2.notelist.SearchableListModel
import pl.wojtach.silvarerum2.room.NotesDao

interface NotesComponent {
    fun notesDao(): NotesDao
    fun readNoteModel(scope: CoroutineScope, noteSnapshot: NoteSnapshot): ReadNoteModel
    fun editNoteModel(scope: CoroutineScope, noteSnapshot: NoteSnapshot): EditNoteModel
    fun noteListModel(scope: CoroutineScope): NoteListModel

    companion object {
        lateinit var container: NotesComponent
    }
}

fun notesComponent() = NotesComponent.container