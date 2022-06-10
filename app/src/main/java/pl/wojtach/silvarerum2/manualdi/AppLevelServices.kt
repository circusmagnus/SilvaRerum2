package pl.wojtach.silvarerum2.manualdi

import android.content.Context
import androidx.room.Room
import pl.wojtach.silvarerum2.room.AppDatabase
import pl.wojtach.silvarerum2.room.NotesDao
import kotlin.reflect.KClass

object AppContextProvider : DiContainer<Context> {

    private lateinit var appContext: Context

    fun init(appContext: Context) {
        this.appContext = appContext
    }

    override fun get(): Context = appContext
}

object AppDatabaseProvider : DiContainer<AppDatabase> by single ({
    Room.databaseBuilder(AppContextProvider.get(), AppDatabase::class.java, AppDatabase.DB_NAME).build()
})

object NotesDaoFactory : DiContainer<NotesDao> by factory({
    AppDatabaseProvider.get().notesDao()
})