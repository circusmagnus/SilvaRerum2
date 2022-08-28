package pl.wojtach.silvarerum2.notelist

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import pl.wojtach.silvarerum2.NoteSnapshot

interface NoteListModel: CoroutineScope {
    val state: StateFlow<List<NoteSnapshot>>
    fun delete(note: NoteSnapshot)
    fun addNew(): NoteSnapshot
    fun reorder(fromPosition: Int, toPosition: Int)
}