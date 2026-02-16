package com.z0b1.weatherstation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.z0b1.weatherstation.data.WeatherResponse
import com.z0b1.weatherstation.data.SatelliteRecording
import com.z0b1.weatherstation.data.WeatherService
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.delay
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.os.Build
import android.app.Activity

class WeatherViewModel : ViewModel() {
    private val _useMockData = MutableStateFlow(false)
    val useMockData: StateFlow<Boolean> = _useMockData

    private val baseUrl = "https://your-name.share.zrok.io" // Replace with your zrok URL
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
    private val service = retrofit.create(WeatherService::class.java)

    private val _weather = MutableStateFlow<WeatherResponse?>(null)
    val weather: StateFlow<WeatherResponse?> = _weather

    private val _history = MutableStateFlow<List<WeatherResponse>>(emptyList())
    val history: StateFlow<List<WeatherResponse>> = _history

    private val _selectedMonth = MutableStateFlow(java.time.LocalDate.now().monthValue)
    val selectedMonth: StateFlow<Int> = _selectedMonth

    val filteredHistory: StateFlow<List<WeatherResponse>> = combine(_history, _selectedMonth) { history, month ->
        history.filter { 
            // Mock data timestamps are "dd" for current implementation, 
            // but for yearly we'll need to store full dates or month info.
            // Let's assume the mock data generation will now include ISO dates.
            try {
                java.time.LocalDate.parse(it.timestamp).monthValue == month
            } catch (e: Exception) {
                true // Fallback
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _satellites = MutableStateFlow<List<SatelliteRecording>>(emptyList())
    val satellites: StateFlow<List<SatelliteRecording>> = _satellites

    private val _showHistory = MutableStateFlow(false)
    val showHistory: StateFlow<Boolean> = _showHistory

    private var lastAlertTemp: Double? = null
    private val ALERT_THRESHOLD = 1.0
    private val RESET_THRESHOLD = 2.0 // Avoid flapping

    init {
        startPolling()
    }

    fun checkFrostAlert(context: Context, currentTemp: Double) {
        if (currentTemp < ALERT_THRESHOLD) {
            if (lastAlertTemp == null || lastAlertTemp!! >= ALERT_THRESHOLD) {
                showNotification(context, "Frost Alert!", "Temperature has dropped to ${currentTemp}°C. Protecting watering systems recommended.")
            }
            lastAlertTemp = currentTemp
        } else if (currentTemp > RESET_THRESHOLD) {
            lastAlertTemp = null // Reset for next event
        }
    }

    private fun showNotification(context: Context, title: String, content: String) {
        val channelId = "frost_alerts"
        val notificationId = 1001

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }

    private fun startPolling() {
        // This is a bit of a hack since ViewModel doesn't have Context usually,
        // but for this simple project we can pass it or use a callback.
        // I'll use the check logic inside the dashboard directly to get context easily.
    }

    fun pollData(context: Context) {
        viewModelScope.launch {
            if (!_useMockData.value) {
                try {
                    val result = service.getLatestWeather()
                    _weather.value = result
                    checkFrostAlert(context, result.temp)
                    _satellites.value = service.getSatellites()
                    if (_showHistory.value) {
                        _history.value = service.getWeatherHistory()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                generateMockData()
                _weather.value?.let { checkFrostAlert(context, it.temp) }
            }
        }
    }
    // here generating random data for mock tests 
    private fun generateMockData() {
        val now = java.time.LocalDate.now()
        _weather.value = WeatherResponse(now.toString(), 24.5, 60.0, 5.2, 120, 18.2, 21.1)
        
        val startOfYear = now.withDayOfYear(1)
        _history.value = List(365) { i ->
            val date = startOfYear.plusDays(i.toLong())
            val baseTemp = 5.0 + Math.random() * 25.0 + (if(date.monthValue in 6..8) 10.0 else 0.0)
            WeatherResponse(
                timestamp = date.toString(),
                temp = baseTemp,
                hum = 40.0 + Math.random() * 40.0,
                speed = 1.0 + Math.random() * 12.0,
                heading = (Math.random() * 360).toInt(),
                soilTemp = baseTemp * 0.8 + 2.0, // Stable underground
                surfTemp = baseTemp * 1.1 - 1.0  // More extreme at surface
            )
        }
    }

    fun setSelectedMonth(month: Int) {
        _selectedMonth.value = month
    }

    fun toggleMockData() {
        _useMockData.value = !_useMockData.value
        if (_useMockData.value) generateMockData()
    }

    fun setView(isHistory: Boolean) {
        _showHistory.value = isHistory
        if (isHistory && !_useMockData.value) {
            refreshHistory()
        }
    }

    private fun refreshHistory() {
        viewModelScope.launch {
            try {
                _history.value = service.getWeatherHistory()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getBaseUrl() = baseUrl
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        
        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF0D0F14)) {
                    WeatherDashboard()
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Frost Alerts"
            val descriptionText = "Notifications for freezing temperatures"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("frost_alerts", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

@Composable
fun WeatherDashboard(viewModel: WeatherViewModel = viewModel()) {
    val weather by viewModel.weather.collectAsState()
    val history by viewModel.filteredHistory.collectAsState()
    val satellites by viewModel.satellites.collectAsState()
    val showHistory by viewModel.showHistory.collectAsState()
    val useMockData by viewModel.useMockData.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit) {
        while(true) {
            viewModel.pollData(context)
            delay(10000)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0F172A) // Fallback color
    ) {
        // Deep Gradient Background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F172A),
                            Color(0xFF1E293B),
                            Color(0xFF0F172A)
                        )
                    )
                )
        )

        Column(modifier = Modifier.padding(20.dp).statusBarsPadding()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Weather", fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color.White, letterSpacing = (-1).sp)
                    Text(
                        text = when {
                            useMockData -> "Mock Environment Enabled"
                            showHistory -> "History • Monthly Overview"
                            else -> "Live • ${weather?.timestamp ?: "Syncing..."}"
                        },
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Box {
                    IconButton(
                        onClick = { expanded = true },
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = Color.White
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Color(0xFF1E293B)).padding(4.dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Live Monitor", color = Color.White) },
                            onClick = {
                                viewModel.setView(false)
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("History Trends", color = Color.White) },
                            onClick = {
                                viewModel.setView(true)
                                expanded = false
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.White.copy(alpha = 0.1f))
                        DropdownMenuItem(
                            text = { Text(if(useMockData) "Exit Mock Mode" else "Simulate Live Data", color = if(useMockData) Color(0xFFF87171) else Color(0xFF818CF8)) },
                            onClick = {
                                viewModel.toggleMockData()
                                expanded = false
                            }
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(32.dp))
            
            if (!showHistory) {
                LiveView(weather, satellites, viewModel.getBaseUrl())
            } else {
                HistoryView(history, selectedMonth, onMonthSelect = { viewModel.setSelectedMonth(it) })
            }
        }
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(32.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.15f),
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(32.dp)
            )
            .padding(24.dp)
    ) {
        Column { content() }
    }
}

@Composable
fun LiveView(weather: WeatherResponse?, satellites: List<SatelliteRecording>, baseUrl: String) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard("Temperature", "${weather?.temp ?: "--"}°", Modifier.weight(1f), Color(0xFFF87171))
            StatCard("Humidity", "${weather?.hum ?: "--"}%", Modifier.weight(1f), Color(0xFF60A5FA))
        }

        Spacer(Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard("Soil (-10cm)", "${weather?.soilTemp ?: "--"}°", Modifier.weight(1f), Color(0xFFB45309))
            StatCard("Surface", "${weather?.surfTemp ?: "--"}°", Modifier.weight(1f), Color(0xFF10B981))
        }

        Spacer(Modifier.height(16.dp))

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Wind Velocity", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("${weather?.speed ?: "0.0"} m/s", fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF34D399)))
                        Spacer(Modifier.width(6.dp))
                        val heading = weather?.heading ?: 0
                        Text("${getCardinalDirection(heading)} Heading", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
                    }
                }
                CompassView(weather?.heading ?: 0)
            }
        }

        Spacer(Modifier.height(32.dp))
        Text("Satellite Imagery", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp, letterSpacing = 0.5.sp)
        Spacer(Modifier.height(16.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(satellites) { sat ->
                SatelliteCard(sat, baseUrl)
            }
        }
    }
}

fun getCardinalDirection(heading: Int): String {
    val directions = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
    val index = (((heading + 22.5) % 360) / 45).toInt()
    return directions[index]
}

@Composable
fun HistoryView(history: List<WeatherResponse>, selectedMonth: Int, onMonthSelect: (Int) -> Unit) {
    androidx.compose.foundation.lazy.LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            MonthScroller(selectedMonth, onMonthSelect)
        }
        item {
            GraphCard(
                title = "Temperature Evolution",
                data = history.map { it.temp.toFloat() },
                labels = history.map { try { java.time.LocalDate.parse(it.timestamp).dayOfMonth.toString() } catch(e:Exception) { "0" } },
                unit = "°C",
                color = Color(0xFFF87171)
            )
        }
        item {
            GraphCard(
                title = "Humidity Trend",
                data = history.map { it.hum.toFloat() },
                labels = history.map { try { java.time.LocalDate.parse(it.timestamp).dayOfMonth.toString() } catch(e:Exception) { "0" } },
                unit = "%",
                color = Color(0xFF60A5FA)
            )
        }
        item {
            WindRoseCard(history)
        }
        item {
            GraphCard(
                title = "Ground & Surface Temperature",
                data = history.map { it.soilTemp.toFloat() }, // Soil as primary
                labels = history.map { try { java.time.LocalDate.parse(it.timestamp).dayOfMonth.toString() } catch(e:Exception) { "0" } },
                unit = "°C",
                color = Color(0xFFB45309)
            )
        }
        item {
            WindHistoryCard(history)
        }
    }
}

@Composable
fun MonthScroller(selectedMonth: Int, onMonthSelect: (Int) -> Unit) {
    val months = listOf("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC")
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 8.dp)
    ) {
        itemsIndexed(months) { index, month ->
            val monthNum = index + 1
            val isSelected = selectedMonth == monthNum
            
            Surface(
                onClick = { onMonthSelect(monthNum) },
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) Color(0xFF6366F1) else Color.White.copy(alpha = 0.05f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, 
                    if (isSelected) Color.Transparent else Color.White.copy(alpha = 0.1f)
                ),
                modifier = Modifier.width(60.dp)
            ) {
                Text(
                    text = month,
                    modifier = Modifier.padding(vertical = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun WindRoseCard(history: List<WeatherResponse>) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Text("Wind Direction Distribution", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text("Rose Graph", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(16.dp))
        WindRoseGraph(history.map { it.heading }, Modifier.height(200.dp).fillMaxWidth())
    }
}

@Composable
fun WindRoseGraph(headings: List<Int>, modifier: Modifier) {
    if (headings.isEmpty()) return
    val bins = IntArray(8) { 0 }
    headings.forEach { h ->
        val index = (((h + 22.5) % 360) / 45).toInt()
        bins[index]++
    }
    val maxBin = bins.maxOrNull() ?: 1

    Canvas(modifier = modifier) {
        val center = size.center
        val maxRadius = size.minDimension / 2 * 0.9f
        
        // Scale Circles
        for (i in 1..4) {
            drawCircle(Color.White.copy(alpha = 0.05f), radius = maxRadius * (i / 4f), style = Stroke(1f))
        }

        // Sectors
        bins.forEachIndexed { i, count ->
            val startAngle = (i * 45f - 22.5f - 90f)
            val sweepAngle = 42f
            val radius = (count.toFloat() / maxBin) * maxRadius
            
            val path = Path().apply {
                moveTo(center.x, center.y)
                val rect = androidx.compose.ui.geometry.Rect(
                    center.x - radius, center.y - radius,
                    center.x + radius, center.y + radius
                )
                arcTo(rect, startAngle, sweepAngle, false)
                close()
            }
            drawPath(
                path, 
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF6366F1), Color(0xFF6366F1).copy(alpha = 0.2f)),
                    center = center,
                    radius = radius.coerceAtLeast(1f)
                )
            )
        }
    }
}

@Composable
fun GraphCard(title: String, data: List<Float>, labels: List<String>, unit: String, color: Color) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Text(title, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        PillarChart(data, labels, unit, color, Modifier.height(220.dp).fillMaxWidth())
    }
}

@Composable
fun PillarChart(data: List<Float>, labels: List<String>, unit: String, color: Color, modifier: Modifier) {
    if (data.isEmpty()) return
    val max = data.maxOrNull() ?: 1f
    val min = (data.minOrNull() ?: 0f).coerceAtMost(0f)
    val range = (max - min).coerceAtLeast(0.1f)
    
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val textMeasurer = rememberTextMeasurer()

    Box(modifier = modifier) {
        Canvas(modifier = Modifier
            .fillMaxSize()
            .pointerInput(data) {
                detectTapGestures { offset ->
                    val leftMargin = 50.dp.toPx()
                    val rightMargin = 20.dp.toPx()
                    val spacing = 8f
                    val chartWidth = size.width - leftMargin - rightMargin
                    val pillarWidth = (chartWidth - (spacing * (data.size - 1))) / data.size.coerceAtLeast(1)
                    
                    val index = ((offset.x - leftMargin) / (pillarWidth + spacing)).toInt()
                    if (index in data.indices) {
                        selectedIndex = index
                    }
                }
            }
        ) {
            val width = size.width
            val height = size.height
            val leftMargin = 50.dp.toPx()
            val rightMargin = 20.dp.toPx()
            val bottomMargin = 40.dp.toPx()
            val chartWidth = width - leftMargin - rightMargin
            val chartHeight = height - bottomMargin
            
            val spacing = 6f
            val pillarWidth = (chartWidth - (spacing * (data.size - 1))) / data.size.coerceAtLeast(1)

            // Draw Y-Axis Value Labels (Min, Mid, Max)
            val yLabels = listOf(min, (min + max) / 2, max)
            yLabels.forEach { valNum ->
                val yPos = chartHeight - ((valNum - min) / range * chartHeight)
                drawText(
                    textMeasurer = textMeasurer,
                    text = String.format("%.1f", valNum),
                    topLeft = Offset(5f, yPos - 10.dp.toPx()),
                    style = androidx.compose.ui.text.TextStyle(color = Color.Gray, fontSize = 10.sp)
                )
                // Grid line
                drawLine(Color.Gray.copy(alpha = 0.1f), Offset(leftMargin, yPos), Offset(width - rightMargin, yPos), 1f)
            }

            // Draw Axes
            drawLine(Color.Gray.copy(alpha = 0.5f), Offset(leftMargin, 0f), Offset(leftMargin, chartHeight), 2f)
            drawLine(Color.Gray.copy(alpha = 0.5f), Offset(leftMargin, chartHeight), Offset(width - rightMargin, chartHeight), 2f)

            // Draw Pillars and X-Axis Labels (Date Numbers)
            data.forEachIndexed { index, value ->
                val pillarHeight = ((value - min) / range * chartHeight)
                val x = leftMargin + index * (pillarWidth + spacing)
                val y = chartHeight - pillarHeight
                
                val isSelected = selectedIndex == index
                
                drawRoundRect(
                    color = if (isSelected) Color.White else color.copy(alpha = 0.7f),
                    topLeft = Offset(x, y),
                    size = androidx.compose.ui.geometry.Size(pillarWidth, pillarHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                )

                // Show date every 5th pillar or if first/last
                if (index % 5 == 0 || index == data.size - 1) {
                    val labelText = labels[index]
                    val labelLayoutInfo = textMeasurer.measure(
                        text = labelText,
                        style = androidx.compose.ui.text.TextStyle(fontSize = 10.sp)
                    )
                    drawText(
                        textMeasurer = textMeasurer,
                        text = labelText,
                        topLeft = Offset(x - (labelLayoutInfo.size.width / 2) + (pillarWidth / 2), chartHeight + 5.dp.toPx()),
                        style = androidx.compose.ui.text.TextStyle(color = Color.Gray, fontSize = 10.sp)
                    )
                }
            }
        }
        
        if (selectedIndex != null) {
            val idx = selectedIndex!!
            if (idx in data.indices) {
                Surface(
                    modifier = Modifier.align(Alignment.TopCenter).padding(8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF6366F1),
                    tonalElevation = 8.dp
                ) {
                    Column(Modifier.padding(8.dp)) {
                        val valueFormatted = String.format("%.1f", data[idx])
                        Text("$valueFormatted$unit", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Day ${labels[idx]}", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun WindHistoryCard(history: List<WeatherResponse>) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Wind Trail", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("Direction Scatter", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
            }
            WindVaneHistoryGraph(history.map { it.heading }, Modifier.size(120.dp))
        }
    }
}

@Composable
fun WindVaneHistoryGraph(headings: List<Int>, modifier: Modifier) {
    Canvas(modifier = modifier) {
        val center = size.center
        val radius = size.minDimension / 2
        drawCircle(color = Color(0xFF2D324A), radius = radius, style = Stroke(2f))
        
        headings.forEachIndexed { index, heading ->
            val angle = (heading - 90).toFloat()
            val alpha = (index.toFloat() / headings.size).coerceIn(0.1f, 1f)
            val rad = Math.toRadians(angle.toDouble())
            val x = center.x + Math.cos(rad).toFloat() * radius * 0.8f
            val y = center.y + Math.sin(rad).toFloat() * radius * 0.8f
            drawCircle(color = Color(0xFF6366F1).copy(alpha = alpha), radius = 6f, center = Offset(x, y))
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier, tint: Color) {
    GlassCard(modifier = modifier) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(tint))
        }
        Spacer(Modifier.height(16.dp))
        Text(label, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(value, fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
    }
}

@Composable
fun CompassView(heading: Int) {
    Canvas(modifier = Modifier.size(100.dp)) {
        val center = size.center
        val radius = size.minDimension / 2
        
        // Background Ring
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = 0.05f), Color.Transparent)
            ),
            radius = radius
        )
        drawCircle(color = Color.White.copy(alpha = 0.1f), radius = radius, style = Stroke(2f))
        
        // Direction Labels (Cardinal & Intercardinal)
        val labels = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
        val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            alpha = 150
            textSize = 10.sp.toPx()
            textAlign = android.graphics.Paint.Align.CENTER
            isFakeBoldText = true
        }
        
        labels.forEachIndexed { index, label ->
            val angle = index * 45f
            val labelRad = Math.toRadians((angle - 90).toDouble())
            val lx = center.x + Math.cos(labelRad).toFloat() * (radius - 15.dp.toPx())
            val ly = center.y + Math.sin(labelRad).toFloat() * (radius - 15.dp.toPx()) + 5.dp.toPx() // Vertical adjustment
            
            drawContext.canvas.nativeCanvas.drawText(label, lx, ly, textPaint)
        }
        
        rotate(heading.toFloat()) {
            // Main Needle
            val needlePath = Path().apply {
                moveTo(center.x, center.y - radius + 5f)
                lineTo(center.x - 8f, center.y)
                lineTo(center.x + 8f, center.y)
                close()
            }
            drawPath(needlePath, Color(0xFF6366F1))
            
            drawCircle(
                color = Color.White,
                radius = 4f,
                center = center
            )
        }
    }
}

@Composable
fun SatelliteCard(sat: SatelliteRecording, baseUrl: String) {
    Column(modifier = Modifier.width(200.dp)) {
        Box(Modifier.height(140.dp).fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.Black)) {
            if (sat.images.isNotEmpty()) {
                AsyncImage(
                    model = "$baseUrl/view-recordings/${sat.path}/${sat.images[0]}",
                    contentDescription = sat.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Text(sat.name.uppercase(), Modifier.align(Alignment.BottomStart).padding(8.dp), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}
