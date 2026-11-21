package com.example.waagent

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

/**
 * Android calls onNotificationPosted() for EVERY notification from EVERY app once the
 * user grants "notification access". So the first job is to throw away everything that
 * is not WhatsApp before looking at any content.
 */
class WaListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // Privacy rule: ignore every other app without reading a single word of it.
        if (sbn.packageName != WHATSAPP) return

        val n = sbn.notification

        // Skip the "3 new messages" bundle row and WhatsApp's background-check notice.
        if ((n.flags and Notification.FLAG_GROUP_SUMMARY) != 0) return
        if ((n.flags and Notification.FLAG_ONGOING_EVENT) != 0) return

        val extras = n.extras
        val chat = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: "?"
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: return

        // The "reply box" is the notification action that carries a RemoteInput.
        val replyAction = n.actions?.firstOrNull { a -> a.remoteInputs?.isNotEmpty() == true }

        MessageHub.add(Captured(chat, text, sbn.postTime), replyAction)
    }

    companion object {
        const val WHATSAPP = "com.whatsapp"
    }
}
