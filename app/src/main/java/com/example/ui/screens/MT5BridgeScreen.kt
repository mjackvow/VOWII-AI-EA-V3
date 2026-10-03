package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SignalAction
import com.example.ui.TradingViewModel
import com.example.ui.components.GlassmorphicCard
import com.example.ui.theme.*

@Composable
fun MT5BridgeScreen(viewModel: TradingViewModel) {
    val mt5Connected by viewModel.mt5Connected.collectAsState()
    val savedLogin by viewModel.mt5Login.collectAsState()
    val savedPassword by viewModel.mt5Password.collectAsState()
    val savedServer by viewModel.mt5Server.collectAsState()
    val savedTermsAccepted by viewModel.mt5TermsAccepted.collectAsState()
    val credentialsSaved by viewModel.mt5CredentialsSaved.collectAsState()
    val riskPct by viewModel.riskPercentage.collectAsState()
    val autoExec by viewModel.autoExecutionEnabled.collectAsState()
    val logs by viewModel.executionLogs.collectAsState()

    var showGuideModal by remember { mutableStateOf(false) }

    // MT5 Credentials Input Form State
    var loginInput by remember(savedLogin) { mutableStateOf(savedLogin) }
    var passwordInput by remember(savedPassword) { mutableStateOf(savedPassword) }
    var serverInput by remember(savedServer) { mutableStateOf(savedServer) }
    var termsAcceptedInput by remember(savedTermsAccepted) { mutableStateOf(savedTermsAccepted) }
    var passwordVisible by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MT5 BRIDGE TERMINAL",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
                        ),
                        color = TextPrimary
                    )
                    Text(
                        text = "Direct MetaTrader 5 account connection and signal bridge",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Button(
                    onClick = { showGuideModal = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x3300FF88), contentColor = NeonGreen),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("SETUP GUIDE", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Connection Status Card
        item {
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = if (mt5Connected) NeonGreen else NeonRed
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(if (mt5Connected) NeonGreen else NeonRed)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (mt5Connected) "MT5 TERMINAL CONNECTED" else "DISCONNECTED",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (mt5Connected) NeonGreen else NeonRed
                                )
                                Text(text = "Server: $savedServer (Ping: 12ms)", fontSize = 11.sp, color = TextMuted)
                            }
                        }

                        IconButton(onClick = { viewModel.toggleMT5Connection() }) {
                            Icon(
                                imageVector = Icons.Default.PowerSettingsNew,
                                contentDescription = null,
                                tint = if (mt5Connected) NeonGreen else NeonRed
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        AccountStat("Account #", savedLogin)
                        AccountStat("Equity", "$24,850.00", NeonGreen)
                        AccountStat("Leverage", "1:500")
                        AccountStat("Free Margin", "$21,400.00")
                    }
                }
            }
        }

        // MT5 Login Credentials Section
        item {
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = NeonGreen
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AccountBalance, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "MT5 ACCOUNT LOGIN CREDENTIALS",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0x2200FF88)
                        ) {
                            Text(
                                text = "LIVE ACC",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Enter your MetaTrader 5 trading account credentials to authorize live execution",
                        fontSize = 11.sp,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 1. Login ID Field
                    Text(
                        text = "ACCOUNT LOGIN ID",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = loginInput,
                        onValueChange = { loginInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("e.g. 8849201", color = TextMuted, fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.AccountBalance, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(18.dp))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = SurfaceCardBorder,
                            focusedContainerColor = Color(0x1A000000),
                            unfocusedContainerColor = Color(0x1A000000)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Password Field
                    Text(
                        text = "MT5 TRADING PASSWORD",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("Enter account password", color = TextMuted, fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = SurfaceCardBorder,
                            focusedContainerColor = Color(0x1A000000),
                            unfocusedContainerColor = Color(0x1A000000)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. Server Name Field
                    Text(
                        text = "BROKER SERVER NAME",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = serverInput,
                        onValueChange = { serverInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("e.g. Deriv-Server-01, Exness-Real", color = TextMuted, fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Dns, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(18.dp))
                        },
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = SurfaceCardBorder,
                            focusedContainerColor = Color(0x1A000000),
                            unfocusedContainerColor = Color(0x1A000000)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Terms & Conditions Acceptance Checkbox
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x1A000000))
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Checkbox(
                            checked = termsAcceptedInput,
                            onCheckedChange = { termsAcceptedInput = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = NeonGreen,
                                uncheckedColor = TextMuted,
                                checkmarkColor = CanvasBackground
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "I accept the T&Cs and automated execution risk agreement for MT5 account login.",
                            fontSize = 11.sp,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Connect Button
                    Button(
                        onClick = {
                            viewModel.updateMT5Credentials(loginInput, passwordInput, serverInput, termsAcceptedInput)
                        },
                        enabled = termsAcceptedInput && loginInput.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonGreen,
                            contentColor = CanvasBackground,
                            disabledContainerColor = Color(0x3300FF88),
                            disabledContentColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (termsAcceptedInput) "CONNECT & LOGIN TO MT5 TERMINAL" else "ACCEPT T&CS TO LOGIN",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = CanvasBackground
                        )
                    }

                    if (credentialsSaved && savedLogin.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x2200FF88))
                                .border(1.dp, Color(0x4400FF88), RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = NeonGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Credentials saved • Account #$savedLogin ($savedServer)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonGreen
                            )
                        }
                    }
                }
            }
        }

        // NOFX Autopilot & Hard Risk Shield (from NoFxAiOS/nofx)
        item {
            val shield by viewModel.autopilotShield.collectAsState()

            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = if (shield.enabled) NeonGreen else SurfaceCardBorder
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = if (shield.enabled) NeonGreen else TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "NOFX AUTOPILOT RISK SHIELD",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Hard risk limits & multi-exchange execution",
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Switch(
                            checked = shield.enabled,
                            onCheckedChange = { viewModel.toggleAutopilot() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CanvasBackground,
                                checkedTrackColor = NeonGreen
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        AccountStat("MAX DAILY DD", "${shield.maxDailyDrawdownPct}%", NeonRed)
                        AccountStat("RISK / TRADE", "${shield.portfolioRiskPerTradePct}%", NeonGold)
                        AccountStat("DAILY P&L", "+$${shield.dailyPnlUsd}", NeonGreen)
                        AccountStat("ORDERS", "${shield.totalTradesExecuted} Executed", Color.White)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "ACTIVE EXCHANGES: Binance Spot API (REST/WS) • Alpaca Paper V2 • Deriv MT5 Bridge",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        // Multi-Model Brain Pre-Execution Audit
        item {
            val session by viewModel.modelsAnalysisSession.collectAsState()
            val focusMarket by viewModel.modelsFocusMarket.collectAsState()
            val availableMarkets = viewModel.availableMarkets
            var marketDropdownOpen by remember { mutableStateOf(false) }

            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = NeonCyan
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "MULTI-MODEL BRAIN AUDIT",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "8 Models verify consensus prior to MT5 terminal dispatch",
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = NeonCyan.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "PRE-FLIGHT",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = NeonCyan
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Market Selector Dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0x1AFFFFFF),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { marketDropdownOpen = true }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "MARKET AUDIT: $focusMarket",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.White
                                )
                                Text(text = "Change ▼", fontSize = 11.sp, color = NeonCyan)
                            }
                        }

                        DropdownMenu(
                            expanded = marketDropdownOpen,
                            onDismissRequest = { marketDropdownOpen = false },
                            modifier = Modifier
                                .background(Color(0xFF0E111A))
                                .border(1.dp, SurfaceCardBorder, RoundedCornerShape(8.dp))
                        ) {
                            availableMarkets.forEach { m ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = m,
                                            fontSize = 12.sp,
                                            fontWeight = if (m == focusMarket) FontWeight.Bold else FontWeight.Normal,
                                            color = if (m == focusMarket) NeonGreen else Color.White
                                        )
                                    },
                                    onClick = {
                                        viewModel.setModelsFocusMarket(m)
                                        marketDropdownOpen = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Run Pre-Flight Audit Button
                    Button(
                        onClick = { viewModel.runMT5PreFlightCheck() },
                        enabled = !session.isRunning,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0x3300E5FF),
                            contentColor = NeonCyan
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (session.isRunning) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = NeonCyan, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AUDITING WITH 8 BRAIN MODELS...", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Text("RUN 8-MODEL PRE-FLIGHT AUDIT FOR $focusMarket", fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    // Display Brain Consensus & Model Votes
                    if (session.decisions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))

                        val finalAction = session.finalSignal ?: SignalAction.BUY
                        val actionColor = when (finalAction) {
                            SignalAction.BUY -> NeonGreen
                            SignalAction.SELL -> NeonRed
                            SignalAction.WAIT -> NeonAmber
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = actionColor.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, actionColor.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${finalAction.name} $focusMarket (${session.finalConfidence}% Consensus)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        color = actionColor
                                    )
                                    Text(
                                        text = session.consensusSummary,
                                        fontSize = 10.sp,
                                        color = TextSecondary
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Mini grid of model decisions
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    session.decisions.forEach { d ->
                                        val c = when (d.action) {
                                            SignalAction.BUY -> NeonGreen
                                            SignalAction.SELL -> NeonRed
                                            SignalAction.WAIT -> NeonAmber
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = "${d.modelName}:", fontSize = 10.sp, color = TextMuted)
                                            Text(text = "${d.action.name} (${d.confidence}%)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = c)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = { viewModel.executeModelsFinalSignalOnMT5() },
                                    enabled = mt5Connected && savedTermsAccepted,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = CanvasBackground),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("DISPATCH BRAIN SIGNAL TO MT5 TERMINAL", fontSize = 11.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Risk & Automation Controls
        item {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AUTOMATION & RISK PROFILE",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                        }

                        Switch(
                            checked = autoExec,
                            onCheckedChange = { viewModel.toggleAutoExecution() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CanvasBackground,
                                checkedTrackColor = NeonGreen
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Risk Per Trade: ${String.format("%.1f", riskPct)}% of Account Equity",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )

                    Slider(
                        value = riskPct,
                        onValueChange = { viewModel.setRiskPercentage(it) },
                        valueRange = 0.5f..5.0f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonGreen,
                            activeTrackColor = NeonGreen,
                            inactiveTrackColor = SurfaceCardBorder
                        )
                    )
                }
            }
        }

        // Execution Logs Console
        item {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LIVE MT5 EXECUTION LOGS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextMuted
                        )
                        Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(Color(0x1AFFFFFF), RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        logs.take(12).forEach { logLine ->
                            Text(
                                text = logLine,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = if (logLine.contains("SIGNAL") || logLine.contains("OUT") || logLine.contains("IN") || logLine.contains("LOGIN")) NeonGreen else if (logLine.contains("Error") || logLine.contains("disconnected")) NeonRed else TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }

    // MT5 Setup Guide Dialog
    if (showGuideModal) {
        AlertDialog(
            onDismissRequest = { showGuideModal = false },
            confirmButton = {
                TextButton(onClick = { showGuideModal = false }) {
                    Text("CLOSE", fontWeight = FontWeight.Bold, color = NeonGreen)
                }
            },
            title = {
                Text(
                    text = "HOW TO CONNECT APP & PASTE MQL5 EA FILE IN MT5",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GuideItem(
                        step = "1. How to find the MT5 MQL5/Experts/ Folder:",
                        desc = "• Open MetaTrader 5 on your PC.\n• In top menu, click File -> Open Data Folder (or Ctrl+Shift+D).\n• Double-click MQL5 folder, then open the Experts folder."
                    )
                    GuideItem(
                        step = "2. Create & Compile the .mq5 File:",
                        desc = "• Open MetaEditor in MT5 (press F4).\n• Click 'New' -> Expert Advisor (template) -> name it 'MobileBridgeEA'.\n• Paste the MQL5 code provided in the app -> Click 'Compile'."
                    )
                    GuideItem(
                        step = "3. Enable WebRequest in MT5:",
                        desc = "• In MT5, go to Tools -> Options -> Expert Advisors.\n• Check 'Allow WebRequest for listed URL'.\n• Add URL: https://ais-dev-ql5toc46zwjnsw77gtogtb-897707736535.europe-west2.run.app"
                    )
                    GuideItem(
                        step = "4. Enter Credentials & Attach EA:",
                        desc = "• Enter your MT5 Account Login ID, Password, and Server Name above.\n• Attach MobileBridgeEA from Navigator -> Experts to any chart in MT5."
                    )
                }
            },
            containerColor = Color(0xFF0F131D),
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun GuideItem(step: String, desc: String) {
    Column {
        Text(text = step, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = desc, fontSize = 11.sp, color = TextSecondary)
    }
}

@Composable
private fun AccountStat(label: String, value: String, valueColor: Color = TextPrimary) {
    Column {
        Text(text = label, fontSize = 10.sp, color = TextMuted)
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            fontFamily = FontFamily.Monospace
        )
    }
}

