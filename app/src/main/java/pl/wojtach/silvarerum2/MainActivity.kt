package pl.wojtach.silvarerum2

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalLifecycleOwner
import pl.wojtach.silvarerum2.manualdi.notesDeps
import pl.wojtach.silvarerum2.notelist.NoteListScreen
import pl.wojtach.silvarerum2.parcels.ParcelizedNavigationModel
import pl.wojtach.silvarerum2.parcels.toParcel
import pl.wojtach.silvarerum2.ui.theme.SilvaRerum2Theme
import pl.wojtach.silvarerum2.utils.collectWhileStarted
import pl.wojtach.silvarerum2.utils.parcelable

class MainActivity : ComponentActivity() {

    private lateinit var navigationModel: NavigationModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigationModel =
            if (savedInstanceState != null) {
                savedInstanceState.parcelable<ParcelizedNavigationModel>(NAV_MODEL_KEY)
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
        Log.d("lw", "MainScreen composed")
        val lifecycleOwner = LocalLifecycleOwner.current
        val currentDestination = navigationModel.state.collectWhileStarted(lifecycleOwner)
        Navigation(navigationModel = navigationModel, destination = currentDestination.value)
    }

    @Composable
    private fun Navigation(navigationModel: NavigationModel, destination: Destination) {
        val scope = rememberCoroutineScope()
        val model = remember(key1 = scope) { notesDeps().searchableNoteList(scope) }
        when (destination) {
            Destination.NoteList -> NoteListScreen(
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

    override fun onBackPressed() {
        if (navigationModel.popBackstack().not()) super.onBackPressed()
    }

    companion object {
        const val TAG = "MainActivity"
        const val NAV_MODEL_KEY = "NavModelKey"
    }
}