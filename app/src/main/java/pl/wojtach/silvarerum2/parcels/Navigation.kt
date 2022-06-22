package pl.wojtach.silvarerum2.parcels

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import pl.wojtach.silvarerum2.Destination
import pl.wojtach.silvarerum2.NavigationModel
import pl.wojtach.silvarerum2.NoteId
import pl.wojtach.silvarerum2.NoteSnapshot
import pl.wojtach.silvarerum2.Timestamp

@Parcelize
class ParcelizedNote(val id: String, val timestamp: Long, val content: String) : Parcelable {

    fun toNoteSnapshot() = NoteSnapshot(NoteId(id), Timestamp(timestamp), content)
}

@Parcelize
class ParcelizedDestination(
    val name: String,
    val note: ParcelizedNote?
) : Parcelable {

    fun toDestination() = when (name) {
        Destination.EditNote.destName -> Destination.EditNote(note!!.toNoteSnapshot())
        Destination.ReadNote.destName -> Destination.ReadNote(note!!.toNoteSnapshot())
        Destination.NoteList.name     -> Destination.NoteList
        else                          -> throw IllegalArgumentException("Cannot read destination from parcel")
    }
}

@Parcelize
class ParcelizedNavigationModel(val currentDest: ParcelizedDestination, val backstack: List<ParcelizedDestination>): Parcelable {

    fun toNavigationModel(): NavigationModel = NavigationModel(
        initialDestination = currentDest.toDestination(),
        backstack = backstack.map { it.toDestination() }
    )
}

fun NoteSnapshot.toParcel() = ParcelizedNote(id = noteId.value, timestamp = created.value, content = content)

fun Destination.toParcel() = ParcelizedDestination(
    name = name,
    note = when (this) {
        is Destination.EditNote -> this.note.toParcel()
        is Destination.ReadNote -> this.note.toParcel()
        is Destination.NoteList -> null
    }
)

fun NavigationModel.toParcel() = ParcelizedNavigationModel(
    currentDest = state.value.toParcel(),
    backstack = backStack.map { it.toParcel() }
)