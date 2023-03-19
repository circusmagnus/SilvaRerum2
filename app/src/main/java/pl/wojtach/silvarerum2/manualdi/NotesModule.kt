package pl.wojtach.silvarerum2.manualdi

import kotlinx.coroutines.CoroutineScope
import pl.wojtach.silvarerum2.EditNoteModel
import pl.wojtach.silvarerum2.NoteSnapshot
import pl.wojtach.silvarerum2.ReadNoteModel
import pl.wojtach.silvarerum2.notelist.NoteListModelImpl
import pl.wojtach.silvarerum2.notelist.SearchableListModel
import pl.wojtach.silvarerum2.room.NotesDao

class NotesModule(private val appComponent: AppComponent) : NotesComponent {

    override fun notesDao(): NotesDao = appComponent.appDb().notesDao()

    override fun readNoteModel(scope: CoroutineScope, noteSnapshot: NoteSnapshot): ReadNoteModel =
        ReadNoteModel(scope, noteSnapshot, notesDao())

    override fun editNoteModel(scope: CoroutineScope, noteSnapshot: NoteSnapshot): EditNoteModel {
        return EditNoteModel(scope, noteSnapshot, notesDao())
    }

    override fun noteListModel(scope: CoroutineScope): NoteListModelImpl {
        return NoteListModelImpl(scope, notesDao())
    }
}