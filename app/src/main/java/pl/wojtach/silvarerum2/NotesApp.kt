package pl.wojtach.silvarerum2

import android.app.Application
import android.os.StrictMode
import pl.wojtach.silvarerum2.manualdi.AppContextProvider
import pl.wojtach.silvarerum2.manualdi.AppDatabaseProvider
import pl.wojtach.silvarerum2.manualdi.Injector

class NotesApp: Application() {

    init {
        StrictMode.enableDefaults()
    }

    override fun onCreate() {
        super.onCreate()
        AppContextProvider.init(this)
    }
}