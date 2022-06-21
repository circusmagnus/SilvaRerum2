package pl.wojtach.silvarerum2

import android.app.Application
import android.os.StrictMode
import pl.wojtach.silvarerum2.manualdi.AppDeps
import pl.wojtach.silvarerum2.manualdi.AppModule

class NotesApp: Application() {

    init {
        StrictMode.enableDefaults()
    }

    override fun onCreate() {
        super.onCreate()
        AppDeps.container = AppModule(this)
    }
}