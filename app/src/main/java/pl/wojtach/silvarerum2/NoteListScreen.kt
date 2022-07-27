package pl.wojtach.silvarerum2

import android.util.Log
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.zIndex
import pl.wojtach.silvarerum2.manualdi.notesDeps
import pl.wojtach.silvarerum2.utils.collectWhileStarted
import pl.wojtach.silvarerum2.widgets.AddButton
import pl.wojtach.silvarerum2.widgets.DeleteNoteButton
import pl.wojtach.silvarerum2.widgets.EditNoteButton
import pl.wojtach.silvarerum2.widgets.ReorderableList
import pl.wojtach.silvarerum2.widgets.ShortNote
import pl.wojtach.silvarerum2.widgets.SilvaRerumHeader
import kotlin.math.roundToInt

@Composable
fun NoteListScreen(onNoteClick: (NoteSnapshot) -> Unit, onNoteAdd: (NoteSnapshot) -> Unit, onNoteEdit: (NoteSnapshot) -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val model = remember(key1 = scope) { notesDeps().noteListModel(scope) }
    val currentList by model.state.collectWhileStarted(lifecycleOwner = lifecycleOwner)
    val lazyListState = rememberLazyListState()
    val draggedNote = remember(key1 = currentList) {
        mutableStateOf<NoteSnapshot?>(null)
    }
    val draggedNoteIndex = remember(key1 = currentList) {
        mutableStateOf(0)
    }
    val listCellFactory: @Composable (Modifier, NoteSnapshot) -> Unit = { modifier, note ->
        val draggedAmountY = remember { mutableStateOf(0f) }

        ShortNote(
            modifier = modifier
                .graphicsLayer {
                    translationY = if (note == draggedNote.value) draggedAmountY.value else 0f
                }.zIndex(if (note == draggedNote.value) 1f else 0f)
                .pointerInput(Unit) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = { offset -> draggedNote.value = note; draggedNoteIndex.value = currentList.indexOf(note) },
                        onDrag = { change, offset -> draggedAmountY.value += offset.y },
                        onDragEnd = {
                            val visibleItems = lazyListState.layoutInfo.visibleItemsInfo
                            Log.d("lw", "visibleItems: $visibleItems, draggedNoteIndex: $draggedNoteIndex")
                            val dragged = visibleItems.first { it.index == draggedNoteIndex.value }
                            val hoveredOver = visibleItems.firstOrNull { item -> dragged.offset + draggedAmountY.value.roundToInt() in item.offset..item.offset + item.size }
                            draggedNote.value = null
                            draggedAmountY.value = 0f
                            hoveredOver?.let { model.reorder(draggedNoteIndex.value, it.index) }
                        }
                    )
                },
            note = note,
            onClick = { onNoteClick(note) },
            DeleteButton = { DeleteNoteButton { model.delete(note) } },
            EditButton = { EditNoteButton { onNoteEdit(note) } }
        )
    }

    Log.d("lw", "NoteListScreen composed: $currentList")

    NoteListLayout(
        topBar = { TopAppBar { SilvaRerumHeader() } },
        noteListUi = {
            ReorderableList(
                lazyListState = lazyListState,
                reorderableItems = currentList,
                ListCell = listCellFactory
            )
        },
        floatingButton = {
            AddButton {
                val newNote = model.addNew()
                onNoteAdd(newNote)
            }
        }
    )
}

@Composable
fun NoteListLayout(topBar: @Composable () -> Unit, noteListUi: @Composable () -> Unit, floatingButton: @Composable () -> Unit) {

    Log.d("lw", "NoteListLayout composed")

    Scaffold(
        topBar = topBar,
        floatingActionButton = floatingButton,
        content = { noteListUi() }
    )
}

