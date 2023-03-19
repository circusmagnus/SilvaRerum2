package pl.wojtach.silvarerum2

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalLifecycleOwner
import pl.wojtach.silvarerum2.manualdi.notesComponent
import pl.wojtach.silvarerum2.notelist.NoteListScreen
import pl.wojtach.silvarerum2.parcels.ParcelizedNavigationModel
import pl.wojtach.silvarerum2.parcels.toParcel
import pl.wojtach.silvarerum2.ui.theme.SilvaRerum2Theme
import pl.wojtach.silvarerum2.utils.collectWhileStarted

class MainActivity : ComponentActivity() {

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
        Log.d("lw", "MainScreen composed")
        val navigationSaver = object : Saver<NavigationModel, ParcelizedNavigationModel> {
            override fun restore(value: ParcelizedNavigationModel): NavigationModel = value.toNavigationModel()

            override fun SaverScope.save(value: NavigationModel): ParcelizedNavigationModel = value.toParcel()
        }
        val (navigationModel, _) = rememberSaveable(stateSaver = navigationSaver) {
            mutableStateOf(NavigationModel())
        }
        val lifecycleOwner = LocalLifecycleOwner.current
        val currentDestination = navigationModel.state.collectWhileStarted(lifecycleOwner)
        Navigation(navigationModel = navigationModel, destination = currentDestination.value)
    }

    @Composable
    private fun Navigation(navigationModel: NavigationModel, destination: Destination) {
        setupBackPress(navigationModel)
        val scope = rememberCoroutineScope()
        val model = remember(key1 = scope) { notesComponent().noteListModel(scope) }
        when (destination) {
            is Destination.NoteList -> NoteListScreen(
                model = model,
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

    private fun setupBackPress(navigationModel: NavigationModel) {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!navigationModel.popBackstack()) {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }
}