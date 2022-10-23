package pl.wojtach.silvarerum2.manualdi

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import pl.wojtach.silvarerum2.room.AppDatabase

interface AppComponent {
    fun appContext(): Context
    fun appScope(): CoroutineScope
    fun appDb(): AppDatabase

    companion object {
        lateinit var container: AppComponent
    }
}

fun appComponent() = AppComponent.container