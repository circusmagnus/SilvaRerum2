package pl.wojtach.silvarerum2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import pl.wojtach.silvarerum2.ui.theme.SilvaRerum2Theme
import pl.wojtach.silvarerum2.ui.theme.Typography
import androidx.compose.runtime.getValue


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val notes = Notes()
        setContent {
            SilvaRerum2Theme {
                val navigationEvent by notes.events.getFor(TAG).collectAsState(initial = NavigationEvent.ShowList)
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    ShowNotes(notes = notes, event = navigationEvent)
                }
            }
        }
    }

    @Composable
    private fun ShowNotes(notes: Notes, event: NavigationEvent) {
        when(event) {
            NavigationEvent.ShowList -> NoteListScreen(notes = notes)
            is NavigationEvent.NoteClicked -> ReadOnlyNoteScreen(noteId = event.noteId, notes = notes)
            else -> throw NotImplementedError()
        }
    }

    companion object {
        const val TAG = "MainActivity"
    }
}



@Composable
fun EditNote(title: String, content: String){
    Column {
        TextField(value = title, onValueChange = {}, label = { Text("Tytuł") })
        TextField(value = content, onValueChange = {}, label = { Text("Treść") })
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SilvaRerum2Theme {
        EditNote(title = "", content = "coś tu")
    }
}