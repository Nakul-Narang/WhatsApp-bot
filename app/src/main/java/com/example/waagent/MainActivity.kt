package com.example.waagent

import android.app.Activity
import android.app.AlertDialog
import android.app.Notification
import android.app.PendingIntent
import android.app.RemoteInput
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * M0 screen: shows the permission state, the last captured WhatsApp messages, and one
 * button that sends a fixed test reply. The button opens an approval dialog first,
 * because the spec rule is: nothing is sent without your tap.
 */
class MainActivity : Activity() {

    private lateinit var status: TextView
    private lateinit var log: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            // Android 15 draws apps edge to edge; this keeps content clear of the status bar.
            fitsSystemWindows = true
        }

        status = TextView(this).apply { textSize = 16f; setPadding(24, 24, 24, 24) }

        val openSettings = Button(this).apply {
            text = "Open notification access settings"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
        }

        val replyButton = Button(this).apply {
            text = "Send test reply to latest chat"
            setOnClickListener { confirmAndReply() }
        }

        log = TextView(this).apply { textSize = 14f; setPadding(24, 24, 24, 24) }
        val scroll = ScrollView(this).apply { addView(log) }

        root.addView(status)
        root.addView(openSettings)
        root.addView(replyButton)
        root.addView(scroll, LinearLayout.LayoutParams(MATCH_PARENT, 0, 1f))
        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        MessageHub.onChange = { runOnUiThread { render() } }
        render()
    }

    override fun onPause() {
        super.onPause()
        MessageHub.onChange = null
    }

    private fun listenerEnabled(): Boolean {
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return flat?.contains(packageName) == true
    }

    private fun render() {
        val access = if (listenerEnabled()) "ON" else "OFF - tap the button below and allow this app"
        val target = MessageHub.lastChat?.let { "Reply target: $it" } ?: "Reply target: none yet"
        status.text = "Notification access: $access\n$target"

        val fmt = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        log.text = if (MessageHub.items.isEmpty()) {
            "No WhatsApp messages captured yet.\nSend a message to this phone from another number."
        } else {
            MessageHub.items.reversed().joinToString("\n\n") { m ->
                "${fmt.format(Date(m.time))}  ${m.chat}\n${m.text}"
            }
        }
    }

    /** The approval gate. Nothing is sent unless you press Send in this dialog. */
    private fun confirmAndReply() {
        val chat = MessageHub.lastChat
        val action = MessageHub.lastReplyAction
        if (chat == null || action == null) {
            Toast.makeText(this, "No chat with a reply box yet. Message this phone first.", Toast.LENGTH_LONG).show()
            return
        }
        AlertDialog.Builder(this)
            .setTitle("Approve send?")
            .setMessage("Send \"$TEST_REPLY\" to $chat?")
            .setPositiveButton("Send") { _, _ -> sendReply(action, TEST_REPLY) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /** Fills the notification's reply box with our text and fires it. */
    private fun sendReply(action: Notification.Action, text: String) {
        val inputs = action.remoteInputs
        if (inputs == null) {
            Toast.makeText(this, "This notification has no reply box.", Toast.LENGTH_LONG).show()
            return
        }
        val fill = Intent()
        val results = Bundle()
        for (input in inputs) results.putCharSequence(input.resultKey, text)
        RemoteInput.addResultsToIntent(inputs, fill, results)
        try {
            action.actionIntent.send(this, 0, fill)
            Toast.makeText(this, "Fired. Check the other phone.", Toast.LENGTH_LONG).show()
        } catch (e: PendingIntent.CanceledException) {
            // Happens when WhatsApp or you already cleared that notification.
            Toast.makeText(this, "Failed: that reply box has expired.", Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        const val TEST_REPLY = "M0 test reply"
    }
}
