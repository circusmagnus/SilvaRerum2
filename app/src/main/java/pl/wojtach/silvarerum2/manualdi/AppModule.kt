package pl.wojtach.silvarerum2.manualdi

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import pl.wojtach.silvarerum2.room.AppDatabase

class AppModule(private val appContext: Context) : AppComponent {

    private val appDb: AppDatabase by lazy {
        Room.databaseBuilder(appContext, AppDatabase::class.java, AppDatabase.DB_NAME).build()
    }

    private val appScope by lazy { CoroutineScope(Dispatchers.Default + SupervisorJob()) }

    override fun appContext(): Context = appContext

    override fun appScope(): CoroutineScope = appScope

    override fun appDb(): AppDatabase = appDb
}