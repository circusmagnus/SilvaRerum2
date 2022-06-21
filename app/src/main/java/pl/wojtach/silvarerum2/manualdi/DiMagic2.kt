package pl.wojtach.silvarerum2.manualdi

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import pl.wojtach.silvarerum2.EditNoteModel
import pl.wojtach.silvarerum2.NoteListModel
import pl.wojtach.silvarerum2.NoteSnapshot
import pl.wojtach.silvarerum2.ReadNoteModel
import pl.wojtach.silvarerum2.room.AppDatabase
import pl.wojtach.silvarerum2.room.NotesDao

class AppModule(private val appContext: Context) : AppDeps {

    private val appDb: AppDatabase by lazy {
        Room.databaseBuilder(appContext, AppDatabase::class.java, AppDatabase.DB_NAME).build()
    }

    private val appScope by lazy { CoroutineScope(Dispatchers.Default + SupervisorJob()) }

    override fun appContext(): Context = appContext

    override fun appScope(): CoroutineScope = appScope

    override fun appDb(): AppDatabase = appDb
}

interface AppDeps {
    fun appContext(): Context
    fun appScope(): CoroutineScope
    fun appDb(): AppDatabase

    companion object {
        lateinit var container: AppDeps
    }
}

fun appDeps() = AppDeps.container

interface NotesDeps {
    fun notesDao(): NotesDao
    fun readNoteModel(scope: CoroutineScope, noteSnapshot: NoteSnapshot): ReadNoteModel
    fun editNoteModel(scope: CoroutineScope, noteSnapshot: NoteSnapshot): EditNoteModel
    fun noteListModel(scope: CoroutineScope): NoteListModel

    companion object {
        lateinit var container: NotesDeps
    }
}

class NotesModule(private val appDeps: AppDeps) : NotesDeps {

    override fun notesDao(): NotesDao = appDeps.appDb().notesDao()

    override fun readNoteModel(scope: CoroutineScope, noteSnapshot: NoteSnapshot): ReadNoteModel =
        ReadNoteModel(scope, noteSnapshot, notesDao())

    override fun editNoteModel(scope: CoroutineScope, noteSnapshot: NoteSnapshot): EditNoteModel {
        return EditNoteModel(scope, noteSnapshot, notesDao())
    }

    override fun noteListModel(scope: CoroutineScope): NoteListModel {
        return NoteListModel(scope, notesDao())
    }
}

fun notesDeps() = NotesDeps.container