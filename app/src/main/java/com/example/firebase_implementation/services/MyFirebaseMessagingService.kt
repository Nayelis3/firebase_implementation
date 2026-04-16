package com.example.firebase_implementation.services

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.firebase_implementation.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

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
            Handler(Looper.getMainLooper()).post {
                MainActivity.FCMMessageManager.messageTitle.value = title ?: "Sin título"
                MainActivity.FCMMessageManager.messageBody.value = body ?: "Sin mensaje"
            }
        }
    }
}
