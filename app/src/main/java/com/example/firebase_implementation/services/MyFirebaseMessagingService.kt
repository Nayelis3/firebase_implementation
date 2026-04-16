package com.example.firebase_implementation.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.firebase_implementation.MainActivity
import com.example.firebase_implementation.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_TOKEN", "Nuevo token: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"]
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"]

        Log.d("FCM_MESSAGE", "Recibido - Título: $title, Cuerpo: $body")

        if (title != null || body != null) {
            // Actualizar UI
            Handler(Looper.getMainLooper()).post {
                MainActivity.FCMMessageManager.messageTitle.value = title ?: "Sin título"
                MainActivity.FCMMessageManager.messageBody.value = body ?: "Sin mensaje"
            }
            // Mostrar notificación física
            showNotification(title, body)
        }
    }

    private fun showNotification(title: String?, body: String?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("title", title)
            putExtra("body", body)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, "fcm_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Icono por defecto
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(Random.nextInt(), notificationBuilder.build())
    }
}
