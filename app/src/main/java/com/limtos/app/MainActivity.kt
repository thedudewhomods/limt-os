package com.limtos.app

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

data class GeneratedFile(val name: String, val content: String)
data class ChatLine(val role: String, val text: String, val files: List<GeneratedFile> = emptyList())
data class ChatReply(val text: String, val files: List<GeneratedFile> = emptyList())

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
    val context = LocalContext.current
    val messages = remember {
        mutableStateListOf(ChatLine("assistant", "Welcome to Limt OS. Scan your device details or ask me to generate an OS project."))
    }
    var input by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var pendingFile by remember { mutableStateOf<GeneratedFile?>(null) }
    val scope = rememberCoroutineScope()
    val info = remember { deviceInfo() }

    val saveFile = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        val file = pendingFile
        if (uri != null && file != null) {
            context.contentResolver.openOutputStream(uri)?.use {
                it.write(file.content.toByteArray(Charsets.UTF_8))
            }
        }
        pendingFile = null
    }

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
                            Column(Modifier.padding(12.dp)) {
                                Text(line.text)
                                line.files.forEach { file ->
                                    Spacer(Modifier.height(6.dp))
                                    OutlinedButton(onClick = {
                                        pendingFile = file
                                        saveFile.launch(file.name.substringAfterLast('/'))
                                    }) {
                                        Text("Save ${file.name}")
                                    }
                                }
                            }
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
                            val history = messages.map { JSONObject().put("role", it.role).put("content", it.text) }
                            messages.add(ChatLine("user", prompt))
                            busy = true
                            scope.launch {
                                val answer = withContext(Dispatchers.IO) { sendMessage(prompt, info, history) }
                                messages.add(ChatLine("assistant", answer.text, answer.files))
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

private fun sendMessage(prompt: String, info: JSONObject, history: List<JSONObject>): ChatReply {
    return try {
        val historyJson = JSONArray()
        history.forEach { historyJson.put(it) }
        val payload = JSONObject()
            .put("message", prompt)
            .put("device_info", info)
            .put("history", historyJson)
        val body = payload.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("${BuildConfig.API_BASE_URL.trimEnd('/')}/chat")
            .post(body)
            .build()
        OkHttpClient().newCall(request).execute().use { response ->
            val responseText = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                ChatReply("Backend error: HTTP ${response.code}. Check the backend URL and server logs.")
            } else {
                val json = JSONObject(responseText)
                val fileArray = json.optJSONArray("files") ?: JSONArray()
                val files = (0 until fileArray.length()).mapNotNull { index ->
                    val item = fileArray.optJSONObject(index) ?: return@mapNotNull null
                    val name = item.optString("name")
                    if (name.isBlank()) null else GeneratedFile(name, item.optString("content"))
                }
                ChatReply(json.optString("reply", "No reply returned."), files)
            }
        }
    } catch (e: Exception) {
        ChatReply("Couldn't reach the Limt OS backend. Set API_BASE_URL to your HTTPS server address and try again.")
    }
}
