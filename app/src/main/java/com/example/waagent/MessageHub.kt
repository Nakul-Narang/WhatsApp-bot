package com.example.waagent

import android.app.Notification

/** One captured WhatsApp message. */
data class Captured(val chat: String, val text: String, val time: Long)

/**
 * M0 only: a tiny in-memory notice board shared by the listener service and the screen.
 * Nothing is saved to disk. Close the app and it is gone. (Encrypted storage and the
 * 3-day auto-delete come in milestone M1.)
 */
object MessageHub {
    private const val MAX_ITEMS = 50

    val items = ArrayDeque<Captured>() // newest at the end

    /** The reply box of the most recent chat that had one. */
    var lastReplyAction: Notification.Action? = null
    var lastChat: String? = null

    /** The screen sets this so it can redraw when a new message arrives. */
    var onChange: (() -> Unit)? = null

    fun add(item: Captured, replyAction: Notification.Action?) {
        items.addLast(item)
        if (items.size > MAX_ITEMS) items.removeFirst()
        if (replyAction != null) {
            lastReplyAction = replyAction
            lastChat = item.chat
        }
        onChange?.invoke()
    }
}
