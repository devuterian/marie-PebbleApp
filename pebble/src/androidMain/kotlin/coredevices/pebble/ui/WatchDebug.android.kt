package coredevices.pebble.ui

import android.app.Notification
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.RemoteInput
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import co.touchlab.kermit.Logger
import io.rebble.libpebblecommon.connection.AppContext
import localization.localized
import java.time.LocalTime
import java.time.format.DateTimeFormatter


actual fun postTestNotification(appContext: AppContext) {
    val context = appContext.context
    val title = localized("Test Notification", "테스트 알림")
    val currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
    val body = localized("Test @ $currentTime", "$currentTime 테스트")
    val notificationId = 1000
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
    val notificationChannel = android.app.NotificationChannel(
        "test_channel",
        localized("Test Channel", "테스트 알림"),
        android.app.NotificationManager.IMPORTANCE_DEFAULT
    )
    notificationManager.createNotificationChannel(notificationChannel)
    val broadcastIntent = Intent(appContext.context, TestActionReceiver::class.java)

    val pendingIntent: PendingIntent =
        PendingIntent.getBroadcast(appContext.context, 0, broadcastIntent, FLAG_MUTABLE or FLAG_UPDATE_CURRENT)
    val notificationBuilder = Notification.Builder(context, "test_channel")
        .setContentTitle(title)
        .setContentText(body)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setAutoCancel(true)
        .setColor(0xFF00FF00.toInt())
        .addAction(Notification.Action.Builder(null, localized("Test"), pendingIntent).build())
        .addAction(
            Notification.Action.Builder(null, localized("Reply"), pendingIntent)
                .addRemoteInput(
                    RemoteInput.Builder(REPLY_KEY)
                        .setLabel(localized("Reply"))
                        .setChoices(arrayOf(localized("Choice 1", "선택 1"), localized("Choice 2", "선택 2"), localized("Choice 3", "선택 3")))
                        .build())
                .build()
        )

    notificationManager.notify(notificationId, notificationBuilder.build())
}


private val REPLY_KEY = "key_text_reply"

class TestActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val results = RemoteInput.getResultsFromIntent(intent) ?: return
        val reply = results.getCharSequence(REPLY_KEY)
        Logger.d("TestActionReceiver got $intent with reply '$reply'")
        Toast.makeText(context, localized("Reply: $reply", "답장: $reply"), Toast.LENGTH_LONG).show()
    }
}
