package com.limtos.app

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

data class ChatLine(val role: String, val text: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { LimtApp() }
    }
}

private fun deviceInfo(): JSONObject = JSONObject().apply {
    put("manufacturer", Build.MANUFACTURER)
    put("model", Build.MODEL)
    put("device", Build.DEVICE)
    put("product", Build.PRODUCT)
    put("android_release", Build.VERSION.RELEASE)
    put("sdk", Build.VERSION.SDK_INT)
    put("hardware", Build.HARDWARE)
    put("supported_abis", Build.SUPPORTED_ABIS.joinToString())
    put("bootloader_version", Build.BOOTLOADER)
    put("soc_manufacturer", if (Build.VERSION.SDK_INT >= 31) Build.SOC_MANUFACTURER else "Unavailable")
    put("soc_model", if (Build.VERSION.SDK_INT >= 31) Build.SOC_MODEL else "Unavailable")
    put("serial_number", "Not collected")
    put("bootloader_status", "Unable to detect bootloader")
}

@Composable
fun LimtApp() {
    val messages = remember { mutableStateListOf(ChatLine("assistant", "Welcome to Limt OS. Scan your device details or ask me to generate an OS project.")) }
    var input by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val info = remember { deviceInfo() }

    MaterialTheme(colorScheme = darkColorScheme()) {
        Scaffold { padding ->
            Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                Text("Limt OS", style = MaterialTheme.typography.headlineMedium)
                Text("AI Android project builder", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(12.dp))
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("DEVICE CHECK", style = MaterialTheme.typography.labelLarge)
                        Text("${info.optString("manufacturer")} ${info.optString("model")}")
                        Text("Android ${info.optString("android_release")} • SDK ${info.optInt("sdk")}")
                        Text("SoC: ${info.optString("soc_manufacturer")} ${info.optString("soc_model")}")
                        Text("Bootloader: Unable to detect bootloader")
                        Text("Serial number: Not collected")
                    }
                }
                Spacer(Modifier.height(12.dp))
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(messages) { line ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (line.role == "user") MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(line.text, Modifier.padding(12.dp))
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ask Limt OS to build or explain something…") },
                    enabled = !busy
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        val prompt = input.trim()
                        if (prompt.isNotEmpty() && !busy) {
                            input = ""
                            messages.add(ChatLine("user", prompt))
                            busy = true
                            scope.launch {
                                val answer = withContext(Dispatchers.IO) { sendMessage(prompt, info) }
                                messages.add(ChatLine("assistant", answer))
                                busy = false
                            }
                        }
                    },
                    enabled = input.isNotBlank() && !busy,
                    modifier = Modifier.fillMaxWidth()
                ) { Text(if (busy) "Thinking…" else "Send") }
            }
        }
    }
}

private fun sendMessage(prompt: String, info: JSONObject): String {
    return try {
        val payload = JSONObject().put("message", prompt).put("device_info", info)
        val body = payload.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("${BuildConfig.API_BASE_URL.trimEnd('/')}/chat")
            .post(body)
            .build()
        OkHttpClient().newCall(request).execute().use { response ->
            val text = response.body?.string().orEmpty()
            if (!response.isSuccessful) "Backend error: HTTP ${response.code}. Check the backend URL and server logs."
            else JSONObject(text).optString("reply", "No reply returned.")
        }
    } catch (e: Exception) {
        "Couldn't reach the Limt OS backend. Set API_BASE_URL to your HTTPS server address and try again."
    }
}
