package pl.wojtach.silvarerum2

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import pl.wojtach.silvarerum2.parcels.ParcelizedNavigationModel
import pl.wojtach.silvarerum2.parcels.toParcel
import pl.wojtach.silvarerum2.ui.theme.SilvaRerum2Theme

class MainActivity : ComponentActivity() {

    private lateinit var navigationModel: NavigationModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigationModel =
            if (savedInstanceState != null) {
                savedInstanceState.getParcelable<ParcelizedNavigationModel>(NAV_MODEL_KEY)
                    ?.toNavigationModel()
                    ?: NavigationModel()
            } else NavigationModel()

        setContent {
            SilvaRerum2Theme {
                MainScreen()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(NAV_MODEL_KEY, navigationModel.toParcel())
        super.onSaveInstanceState(outState)
    }

    @Composable
    private fun MainScreen() {
//        val notes = remember(key1 = scope) { notesDeps().notes(scope) }
//        val navController = rememberNavController()

        Log.d("lw", "MainScreen composed")

        val currentDestination = navigationModel.state.collectAsState()

        Navigation(navigationModel = navigationModel, destination = currentDestination.value)

//        LaunchedEffect(key1 = notes, key2 = navController) {
//            notes.events.getFor("MainScreen")
//                .onEach { dest -> navController.navigate(dest.uri) }
//                .launchIn(this)
//        }

//        NavHost(navController = navController, startDestination = NavDestination.NoteList.id) {
//            composable(NavDestination.NoteList.id) { NoteListScreen(notes = notes) }
//            composable(NavDestination.ReadNote.id) { navBackStackEntry ->
//                ReadOnlyNoteScreen(noteId = NoteId(navBackStackEntry.arguments?.getString("noteId")!!), notes = notes)
//            }
//            composable(NavDestination.EditNote.id) { navBackStackEntry ->
//                EditNoteScreen(notes = notes, noteId = NoteId(navBackStackEntry.arguments?.getString("noteId")!!))
//            }
//        }

//        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
//            ShowNotes(navController = navController, event = navigationEvent)
//        }
    }

    @Composable
    private fun Navigation(navigationModel: NavigationModel, destination: Destination) {
        when (destination) {
            Destination.NoteList -> NoteListScreen(
                onNoteAdd = { note -> navigationModel.goTo(Destination.EditNote(note)) },
                onNoteClick = { note -> navigationModel.goTo(Destination.ReadNote(note)) },
                onNoteEdit = { note -> navigationModel.goTo(Destination.EditNote(note)) }
            )
            is Destination.ReadNote -> ReadOnlyNoteScreen(
                note = destination.note,
                toEditing = { note -> navigationModel.goTo(Destination.EditNote(note)) },
                onDeletion = { navigationModel.goTo(Destination.NoteList) }
            )
            is Destination.EditNote -> EditNoteScreen(destination.note)
        }
    }

    override fun onBackPressed() {
        if (navigationModel.popBackstack().not()) super.onBackPressed()
    }

    companion object {
        const val TAG = "MainActivity"
        const val NAV_MODEL_KEY = "NavModelKey"
    }
}