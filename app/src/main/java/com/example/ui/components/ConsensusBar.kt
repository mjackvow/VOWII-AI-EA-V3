package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ModelVote
import com.example.data.model.SignalAction
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun ConsensusBar(
    votes: List<ModelVote>,
    modifier: Modifier = Modifier
) {
    val total = if (votes.isEmpty()) 1 else votes.size
    val buyCount = votes.count { it.action == SignalAction.BUY }
    val sellCount = votes.count { it.action == SignalAction.SELL }
    val waitCount = votes.count { it.action == SignalAction.WAIT }

    val buyWeight = buyCount.toFloat() / total
    val sellWeight = sellCount.toFloat() / total
    val waitWeight = waitCount.toFloat() / total

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MULTI-MODEL ENSEMBLE CONSENSUS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = TextMuted
            )
            Text(
                text = "${(maxOf(buyWeight, sellWeight, waitWeight) * 100).toInt()}% Agreement",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (buyWeight >= 0.6f) NeonGreen else if (sellWeight >= 0.6f) NeonRed else NeonAmber
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Segmented Consensus Meter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Color(0x1AFFFFFF))
        ) {
            if (buyWeight > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(buyWeight)
                        .background(NeonGreen)
                )
            }
            if (sellWeight > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(sellWeight)
                        .background(NeonRed)
                )
            }
            if (waitWeight > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(waitWeight)
                        .background(NeonAmber)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Vote counts legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LegendItem(label = "BUY ($buyCount)", color = NeonGreen)
            LegendItem(label = "SELL ($sellCount)", color = NeonRed)
            LegendItem(label = "WAIT ($waitCount)", color = NeonAmber)
        }
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}
