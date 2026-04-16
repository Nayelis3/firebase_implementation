package com.example.firebase_implementation

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {

    object FCMMessageManager {
        var messageTitle = mutableStateOf("")
        var messageBody = mutableStateOf("")
    }

    // Solicitud de permiso para notificaciones (Android 13+)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) Log.d("PERMISO", "Permiso de notificaciones concedido")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Manejar el mensaje si la app se abre desde una notificación
        handleIntent(intent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FCMTokenScreen()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Manejar el mensaje si la app ya estaba abierta y se recibe un nuevo intent
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent?.extras?.let { extras ->
            val title = extras.getString("title") ?: extras.getString("gcm.notification.title")
            val body = extras.getString("body") ?: extras.getString("gcm.notification.body")
            
            if (title != null || body != null) {
                FCMMessageManager.messageTitle.value = title ?: ""
                FCMMessageManager.messageBody.value = body ?: ""
            }
        }
    }
}

@Composable
fun FCMTokenScreen() {
    var token by remember { mutableStateOf("Obteniendo token...") }

    // Escuchamos el estado global del mensaje
    val title by MainActivity.FCMMessageManager.messageTitle
    val body by MainActivity.FCMMessageManager.messageBody

    LaunchedEffect(Unit) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                token = "Error al obtener token"
                return@addOnCompleteListener
            }
            token = task.result
            Log.d("FCM_TOKEN", token)
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        // --- Sección del Token ---
        Text(text = "FCM Device Token:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = token,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- Sección del Mensaje Recibido ---
        Text(text = "Último Mensaje Recibido:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        // Si hay un mensaje, mostramos una tarjeta
        if (title.isNotEmpty() || body.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = body,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            // Si aún no hay mensajes, mostramos un texto de espera
            Text(
                text = "Esperando mensajes desde el servidor...",
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
