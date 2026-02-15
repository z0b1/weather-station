package com.z0b1.weatherstation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.z0b1.weatherstation.data.WeatherResponse
import com.z0b1.weatherstation.data.SatelliteRecording
import com.z0b1.weatherstation.data.WeatherService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.delay
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF0D0F14)) {
                    WeatherDashboard()
                }
            }
        }
    }
}

@Composable
fun WeatherDashboard() {
    val baseUrl = "http://YOUR_DOMAIN.duckdns.org:8000" // Replace with your actual DuckDNS URL
    val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
    val service = retrofit.create(WeatherService::class.java)

    var weather by remember { mutableStateOf<WeatherResponse?>(null) }
    var satellites by remember { mutableStateOf<List<SatelliteRecording>>(emptyList()) }

    LaunchedEffect(Unit) {
        while(true) {
            try {
                weather = service.getLatestWeather()
                satellites = service.getSatellites()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            delay(10000)
        }
    }

    Column(modifier = Modifier.padding(20.dp)) {
        Text("Blueprint Weather", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        Text("Live Data • ${weather?.timestamp ?: "Syncing..."}", color = Color.Gray, fontSize = 14.sp)
        
        Spacer(Modifier.height(24.dp))
        
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard("Temp", "${weather?.temp ?: "--"}°C", Modifier.weight(1f))
            StatCard("Humidity", "${weather?.hum ?: "--"}%", Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))

        Box(modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF1A1E2E))
            .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Wind Speed", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("${weather?.speed ?: "0.0"} m/s", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("${weather?.heading ?: "0"}° Direction", color = Color.Gray, fontSize = 14.sp)
                }
                CompassView(weather?.heading ?: 0)
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("Satellite Feed", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(12.dp))
        
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(satellites) { sat ->
                SatelliteCard(sat, baseUrl)
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier) {
    Box(modifier = modifier
        .clip(RoundedCornerShape(24.dp))
        .background(Color(0xFF1A1E2E))
        .padding(20.dp)
    ) {
        Column {
            Text(label, color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun CompassView(heading: Int) {
    Canvas(modifier = Modifier.size(80.dp)) {
        drawCircle(color = Color(0xFF2D324A), radius = size.minDimension / 2, style = androidx.compose.ui.graphics.drawscope.Stroke(2f))
        rotate(heading.toFloat()) {
            drawLine(
                brush = Brush.verticalGradient(listOf(Color(0xFF6366F1), Color.Transparent)),
                start = center,
                end = center.copy(y = 10f),
                strokeWidth = 8f
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
