package pl.wojtach.silvarerum2.manualdi

import androidx.activity.ComponentActivity
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

object DepsRegistry {

    private val deps: MutableMap<String, Any> = hashMapOf<String, Any>()

    fun register(key: String, dep: Any) {
        deps[key] = dep
    }

    fun get(key: String) = deps[key]
}

class Injector<PROPERTY>(factory: DiContainer<PROPERTY>) : ReadOnlyProperty<ComponentActivity, PROPERTY> {

    private val prop by lazy { factory.get() }

    override fun getValue(thisRef: ComponentActivity, property: KProperty<*>): PROPERTY = prop
}

interface DiContainer<T> {
    fun get(): T
}

private class Factory<T>(private val create: () -> T) : DiContainer<T> {
    override fun get(): T = create()
}

private class Singleton<T>(private val create: () -> T) : DiContainer<T> {
    private val singleton: T by lazy { create() }

    override fun get(): T = singleton
}

fun <T> single(create: () -> T): DiContainer<T> = Singleton(create)
fun <T> factory(create: () -> T): DiContainer<T> = Factory(create)