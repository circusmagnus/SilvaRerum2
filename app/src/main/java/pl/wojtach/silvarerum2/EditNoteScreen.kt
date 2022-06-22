package pl.wojtach.silvarerum2

import android.util.Log
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import pl.wojtach.silvarerum2.manualdi.notesDeps
import pl.wojtach.silvarerum2.utils.collectWhileStarted
import pl.wojtach.silvarerum2.widgets.SilvaRerumHeader
import pl.wojtach.silvarerum2.widgets.UndoButton

@Composable
fun EditNoteScreen(noteSnapshot: NoteSnapshot) {

    Log.d("lw", "EditNoteScreen composed")
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val model = remember(key1 = scope) { notesDeps().editNoteModel(scope, noteSnapshot) }

    val note by model.state.collectWhileStarted(lifecycleOwner = lifecycleOwner)

    EditNoteLayout(
        TextField = { EditTextField(content = note.content, onValueChange = model::edit ) },
        UndoButton = { UndoButton(model::undo) }
    )

//    EditTextField(content = currentContent.content, onValueChange = { newContent -> model.edit(newContent)} )
}

@Composable
fun EditNoteLayout(TextField: @Composable () -> Unit, UndoButton: @Composable () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar {
                Row(modifier = Modifier.padding(8.dp)) {
                    SilvaRerumHeader(modifier = Modifier.weight(1f, fill = true))
                    UndoButton()
                }
            }
        },
       content =  { TextField() }
    )
}

@Composable
fun EditTextField(content: String, onValueChange: (newContent: String) -> Unit) {
    Log.d("lw", "EditNote composed")

    TextField(value = content, onValueChange = onValueChange, label = { Text("Treść") })
}