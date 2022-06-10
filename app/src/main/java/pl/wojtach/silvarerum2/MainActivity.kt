package pl.wojtach.silvarerum2

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import pl.wojtach.silvarerum2.ui.theme.SilvaRerum2Theme
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import pl.wojtach.silvarerum2.manualdi.Injector
import pl.wojtach.silvarerum2.manualdi.NotesDaoFactory

class MainActivity : ComponentActivity() {

    private val notesDao by Injector(NotesDaoFactory)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SilvaRerum2Theme {
                MainScreen()
            }
        }
    }

    @Composable
    private fun MainScreen() {
        val scope = rememberCoroutineScope()
        val notes = remember(key1 = scope) { Notes(scope, notesDao) }
        val navController = rememberNavController()

        Log.d("lw", "MainScreen composed")
        
        LaunchedEffect(key1 = notes, key2 = navController) {
            notes.events.getFor("MainScreen")
                .onEach { dest -> navController.navigate(dest.uri) }
                .launchIn(this)
        }
        
        NavHost(navController = navController, startDestination = NavDestination.NoteList.id) {
            composable(NavDestination.NoteList.id) { NoteListScreen(notes = notes) }
            composable(NavDestination.ReadNote.id) { navBackStackEntry ->
                ReadOnlyNoteScreen(noteId = NoteId(navBackStackEntry.arguments?.getString("noteId")!!), notes = notes)
            }
            composable(NavDestination.EditNote.id) { navBackStackEntry ->
                EditNoteScreen(notes = notes, noteId = NoteId(navBackStackEntry.arguments?.getString("noteId")!!))
            }
        }

//        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
//            ShowNotes(navController = navController, event = navigationEvent)
//        }
    }

    @Composable
    private fun Navigation(navController: NavController, event: NavDestination?) {
        event?.let { navController.navigate(it.uri) }
    }

    companion object {
        const val TAG = "MainActivity"
    }
}