package pl.wojtach.silvarerum2

import android.app.Application
import android.os.StrictMode
import pl.wojtach.silvarerum2.manualdi.AppComponent
import pl.wojtach.silvarerum2.manualdi.AppModule
import pl.wojtach.silvarerum2.manualdi.NotesComponent
import pl.wojtach.silvarerum2.manualdi.NotesModule
import pl.wojtach.silvarerum2.manualdi.appComponent

class NotesApp: Application() {

    init {
        StrictMode.enableDefaults()
    }

    override fun onCreate() {
        super.onCreate()
        AppComponent.container = AppModule(this)
        NotesComponent.container = NotesModule(appComponent())
    }
}