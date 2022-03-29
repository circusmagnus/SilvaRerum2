package pl.allegro.android.buyers.common.util.events

import java.util.concurrent.CopyOnWriteArraySet

/** Event designed to be delivered / handled only once by a concrete entity,
 * but it can also be delivered / handled by multiple different entities.
 *
 * Keeps track of, who has already handled its content in a Collection
 */
class OneTimeEvent<out T>(private val content: T) {

    private val handlers = CopyOnWriteArraySet<String>()

    /** @param asker Used to identify, whether this object has already handled this Event.
     * DO NOT PASS ACTIVITY, FRAGMENT OR VIEW HERE, AS IT MAY PRODUCE MEMORY LEAK!
     * Use companion object for your class or YourClass::class.java for best effect
     *
     * @return Event content or null if it has been already handled by asker
     */
    fun getIfNotHandled(asker: String): T? = if (handlers.add(asker)) content else null

    /** @param byWhom Used to identify, whether this object has already handled this Event.
     * DO NOT PASS ACTIVITY, FRAGMENT OR VIEW HERE, AS IT MAY PRODUCE MEMORY LEAK!
     * Use companion object for your class or YourClass::class.java for best effect
     *
     * @param function Block to execute if event has not benn yet handled by @byWhom
     *
     * @return Event content or null if it has been already handled by asker
     */
    fun runIfNotHandled(byWhom: String, function: (T) -> Unit) {
        if (handlers.add(byWhom)) function(content)
    }

    fun peek(): T = content
}