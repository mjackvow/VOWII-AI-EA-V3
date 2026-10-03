package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun VowiiHeader(
    neuralKey: String = "NEURAL KEY: I",
    modifier: Modifier = Modifier
) {
    var currentTimeStr by remember { mutableStateOf(getFormattedUtcTime()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTimeStr = getFormattedUtcTime()
            delay(1000L)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(CanvasBackground)
            .padding(top = 12.dp, bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "VOWII",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = Color.White
                )
                Text(
                    text = "ai",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = NeonGreen
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "EA",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = Color.White
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(NeonRed, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = neuralKey,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = currentTimeStr,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = Color(0x1AFFFFFF), thickness = 1.dp)
    }
}

private fun getFormattedUtcTime(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'", Locale.US)
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    return sdf.format(Date())
}
