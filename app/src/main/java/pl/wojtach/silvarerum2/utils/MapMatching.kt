package pl.wojtach.silvarerum2.utils

inline fun <T: HasStableId> List<T>.updateSelected(id: String, update: (T) -> T) = map { withId ->
    if (withId.id == id) update(withId) else withId
}