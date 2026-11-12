package com.limtos.app

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import java.util.UUID

data class GeneratedFile(val name: String, val content: String)
data class ChatLine(val id: String = UUID.randomUUID().toString(), val role: String, val text: String, val files: List<GeneratedFile> = emptyList())
data class ChatReply(val text: String, val files: List<GeneratedFile> = emptyList())

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { LimtApp() }
    }
}

private val LimtPurple = Color(0xFF8B5CF6)
private val LimtBackground = Color(0xFF111113)
private val LimtPanel = Color(0xFF1C1C20)

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LimtApp() {
    val context = LocalContext.current
    val messages = remember {
        mutableStateListOf(ChatLine(role = "assistant", text = "Hi! I'm Limt OS. What are we building today?"))
    }
    var input by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var pendingFile by remember { mutableStateOf<GeneratedFile?>(null) }
    var modelMenuOpen by remember { mutableStateOf(false) }
    var showDeviceInfo by remember { mutableStateOf(false) }
    var models by remember { mutableStateOf(listOf("gpt-5.5")) }
    var selectedModel by remember { mutableStateOf("gpt-5.5") }
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val info = remember { deviceInfo() }

    LaunchedEffect(Unit) {
        val remoteModels = withContext(Dispatchers.IO) { loadModels() }
        if (remoteModels.isNotEmpty()) {
            models = remoteModels
            selectedModel = remoteModels.first()
        }
    }

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

    MaterialTheme(colorScheme = darkColorScheme(
        primary = LimtPurple,
        background = LimtBackground,
        surface = LimtPanel
    )) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = Color(0xFF171719),
                    drawerContentColor = Color.White,
                    drawerShape = RoundedCornerShape(topEnd = 0.dp, bottomEnd = 0.dp)
                ) {
                    Column(Modifier.fillMaxHeight().padding(16.dp)) {
                        Text("Limt OS", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("Your AI workspace", color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(24.dp))
                        NavigationDrawerItem(
                            label = { Text("New chat") },
                            selected = false,
                            icon = { Icon(Icons.Default.Add, contentDescription = null) },
                            onClick = {
                                messages.clear()
                                messages.add(ChatLine(role = "assistant", text = "New conversation. What would you like to build?"))
                                scope.launch { drawerState.close() }
                            }
                        )
                        Spacer(Modifier.height(18.dp))
                        Text("RECENT", style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.padding(start = 12.dp, bottom = 8.dp))
                        NavigationDrawerItem(
                            label = { Text(messages.lastOrNull { it.role == "user" }?.text?.take(28) ?: "Welcome to Limt OS", maxLines = 1) },
                            selected = true,
                            onClick = { scope.launch { drawerState.close() } }
                        )
                        Spacer(Modifier.weight(1f))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.10f))
                        Text("Limt OS • Android", color = Color.Gray, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(12.dp))
                    }
                }
            }
        ) {
            Scaffold(
                containerColor = LimtBackground,
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text("Limt OS", fontWeight = FontWeight.SemiBold)
                                Text("AI project builder", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Open sidebar")
                            }
                        },
                        actions = {
                            Box {
                                TextButton(onClick = { modelMenuOpen = true }) {
                                    Text(selectedModel, color = LimtPurple)
                                }
                                DropdownMenu(expanded = modelMenuOpen, onDismissRequest = { modelMenuOpen = false }) {
                                    models.forEach { model ->
                                        DropdownMenuItem(text = { Text(model) }, onClick = {
                                            selectedModel = model
                                            modelMenuOpen = false
                                        })
                                    }
                                }
                            }
                            IconButton(onClick = { showDeviceInfo = true }) {
                                Icon(Icons.Default.Info, contentDescription = "Device information")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = LimtBackground,
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color.White,
                            actionIconContentColor = Color.White
                        )
                    )
                },
                bottomBar = {
                    Column(Modifier.background(LimtBackground).padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .background(LimtPanel, RoundedCornerShape(26.dp))
                                .padding(start = 14.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = input,
                                onValueChange = { input = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Message Limt OS…", color = Color.Gray) },
                                enabled = !busy,
                                maxLines = 5,
                                shape = RoundedCornerShape(22.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    cursorColor = LimtPurple
                                )
                            )
                            IconButton(
                                onClick = {
                                    val prompt = input.trim()
                                    if (prompt.isNotEmpty() && !busy) {
                                        input = ""
                                        val history = messages.map { JSONObject().put("role", it.role).put("content", it.text) }
                                        messages.add(ChatLine(role = "user", text = prompt))
                                        busy = true
                                        scope.launch {
                                            val answer = withContext(Dispatchers.IO) {
                                                sendMessage(prompt, info, history, selectedModel)
                                            }
                                            messages.add(ChatLine(role = "assistant", text = answer.text, files = answer.files))
                                            busy = false
                                        }
                                    }
                                },
                                enabled = input.isNotBlank() && !busy,
                                modifier = Modifier.size(44.dp).background(if (input.isNotBlank() && !busy) LimtPurple else Color.DarkGray, CircleShape)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
                            }
                        }
                        Text("Limt OS can make mistakes. Check generated code before using it.", color = Color.Gray, style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 6.dp))
                    }
                }
            ) { padding ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    items(messages, key = { it.id }) { line ->
                        val user = line.role == "user"
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = if (user) Arrangement.End else Arrangement.Start) {
                            if (!user) {
                                Box(Modifier.size(30.dp).background(LimtPurple.copy(alpha = 0.18f), CircleShape), contentAlignment = Alignment.Center) {
                                    Text("L", color = LimtPurple, fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.width(10.dp))
                            }
                            Column(
                                Modifier.widthIn(max = 310.dp)
                                    .then(if (user) Modifier.background(Color(0xFF34303F), RoundedCornerShape(20.dp)).padding(14.dp) else Modifier.padding(vertical = 3.dp))
                            ) {
                                if (!user) Text("LIMT OS", color = LimtPurple, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                Text(line.text, color = Color.White, style = MaterialTheme.typography.bodyLarge)
                                line.files.forEach { file ->
                                    Spacer(Modifier.height(8.dp))
                                    OutlinedButton(onClick = {
                                        pendingFile = file
                                        saveFile.launch(file.name.substringAfterLast('/'))
                                    }) { Text("Save " + file.name) }
                                }
                            }
                        }
                    }
                    if (busy) {
                        item {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(Modifier.size(18.dp), color = LimtPurple, strokeWidth = 2.dp)
                                Spacer(Modifier.width(10.dp))
                                Text("Limt OS is thinking…", color = Color.LightGray)
                            }
                        }
                    }
                }
            }
        }

        if (showDeviceInfo) {
            AlertDialog(
                onDismissRequest = { showDeviceInfo = false },
                title = { Text("Device information") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(info.optString("manufacturer") + " " + info.optString("model"))
                        Text("Android " + info.optString("android_release") + " • SDK " + info.optInt("sdk"))
                        Text("Hardware: " + info.optString("hardware"))
                        Text("ABI: " + info.optString("supported_abis"))
                        Text("SoC: " + info.optString("soc_manufacturer") + " " + info.optString("soc_model"))
                        Text("Bootloader: Unable to detect")
                        Text("Serial number: Not collected")
                    }
                },
                confirmButton = { TextButton(onClick = { showDeviceInfo = false }) { Text("Done") } }
            )
        }
    }
}

private fun configuredBaseUrl(): String? {
    val value = BuildConfig.API_BASE_URL.trim().trimEnd('/')
    return value.takeIf { it.startsWith("https://") && !it.contains("YOUR_BACKEND_HOST") }
}

private fun loadModels(): List<String> {
    val base = configuredBaseUrl() ?: return emptyList()
    return try {
        val request = Request.Builder().url("$base/models").get().build()
        OkHttpClient().newCall(request).execute().use { response ->
            if (!response.isSuccessful) emptyList()
            else {
                val array = JSONObject(response.body?.string().orEmpty()).optJSONArray("models") ?: JSONArray()
                (0 until array.length()).map { array.optString(it) }.filter { it.isNotBlank() }
            }
        }
    } catch (_: Exception) { emptyList() }
}

private fun sendMessage(prompt: String, info: JSONObject, history: List<JSONObject>, model: String): ChatReply {
    val base = configuredBaseUrl()
        ?: return ChatReply("Limt OS is not connected yet. The app was built without a backend URL. Deploy the FastAPI backend, then set the LIMT_API_BASE_URL GitHub Actions secret to its HTTPS address and rebuild the APK.")
    return try {
        val historyJson = JSONArray()
        history.forEach { historyJson.put(it) }
        val payload = JSONObject()
            .put("message", prompt)
            .put("model", model)
            .put("device_info", info)
            .put("history", historyJson)
        val body = payload.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder().url("$base/chat").post(body).build()
        OkHttpClient().newCall(request).execute().use { response ->
            val responseText = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val detail = runCatching { JSONObject(responseText).optString("detail") }.getOrNull()
                ChatReply(detail?.takeIf { it.isNotBlank() } ?: "Limt OS backend returned HTTP ${response.code}. Check the server logs.")
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
    } catch (error: Exception) {
        ChatReply("Cannot reach the Limt OS server. Check that its HTTPS address is correct and that the backend is running. Details: ${error.javaClass.simpleName}")
    }
}
