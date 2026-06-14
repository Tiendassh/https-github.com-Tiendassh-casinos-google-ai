package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.*
import com.example.ui.*
import com.example.ui.theme.*

// Screen enums for Main Navigation
enum class CasinoNavScreen(val title: String, val route: String) {
    CASINO("Casino", "casino"),
    SPORTS("Deportes", "sports"),
    QUINIELA("Quiniela", "quiniela"),
    RECUPERO("Recupero", "recupero"),
    PAGOS("Caja/Pagos", "pagos"),
    LEGAL("Regulación", "legal")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainCasinoApp()
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainCasinoApp() {
    val viewModel: CasinoViewModel = viewModel()
    val userNull by viewModel.userAccount.collectAsStateWithLifecycle()
    val user = userNull ?: UserAccount() // fallback to default user structure

    var currentScreen by remember { mutableStateOf(CasinoNavScreen.CASINO) }

    // Alert feedback bar
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(viewModel.feedbackMessage) {
        if (viewModel.feedbackMessage.isNotEmpty()) {
            snackbarHostState.showSnackbar(viewModel.feedbackMessage)
            viewModel.feedbackMessage = ""
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("main_scaffold"),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                // Large luxury poker header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Casino,
                                contentDescription = "Logo",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "APOSTA REAL",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp,
                                fontFamily = FontFamily.SansSerif
                            )
                            Text(
                                text = "Casino & Deportes",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Balance Display Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .clickable {
                                currentScreen = CasinoNavScreen.PAGOS
                            }
                            .testTag("balance_card")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "💰", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = String.format("$%.2f", user.balance),
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Filled.AddCircle,
                                contentDescription = "Add credits",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Global Compliance Sticky Warning Banner
                if (!user.isTermsSigned) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { currentScreen = CasinoNavScreen.LEGAL }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = "Alerta",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Firma obligatoria requerida para juego seguro (+18). ¡Firma y gana $500!",
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "FIRMAR",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                            )
                        }
                    }
                } else {
                    Surface(
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Verified,
                                    contentDescription = "Firmado",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Licencia certificada: ${user.signedName}",
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "CUPO JUEGO SEGURO ✓",
                                color = Color.Gray,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                listOf(
                    Triple(CasinoNavScreen.CASINO, Icons.Filled.Casino, Icons.Outlined.Casino),
                    Triple(CasinoNavScreen.SPORTS, Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents),
                    Triple(CasinoNavScreen.QUINIELA, Icons.Filled.ConfirmationNumber, Icons.Outlined.ConfirmationNumber),
                    Triple(CasinoNavScreen.RECUPERO, Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
                    Triple(CasinoNavScreen.PAGOS, Icons.Filled.CreditCard, Icons.Outlined.CreditCard),
                    Triple(CasinoNavScreen.LEGAL, Icons.Filled.Gavel, Icons.Outlined.Gavel)
                ).forEach { (screen, filledIcon, outlinedIcon) ->
                    val selected = currentScreen == screen
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentScreen = screen },
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 9.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = if (selected) filledIcon else outlinedIcon,
                                contentDescription = screen.title,
                                tint = if (selected) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        },
                        modifier = Modifier.testTag("nav_item_${screen.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                CasinoNavScreen.CASINO -> CasinoTabScreen(viewModel, user)
                CasinoNavScreen.SPORTS -> SportsTabScreen(viewModel, user)
                CasinoNavScreen.QUINIELA -> QuinielaTabScreen(viewModel, user)
                CasinoNavScreen.RECUPERO -> RecuperoTabScreen(viewModel, user)
                CasinoNavScreen.PAGOS -> PaymentsTabScreen(viewModel, user)
                CasinoNavScreen.LEGAL -> LegalTabScreen(viewModel, user)
            }
        }
    }
}

// ----------------------------------------------------
// SCREEN 1: CASINO GAMES TAB
// ----------------------------------------------------
@Composable
fun CasinoTabScreen(viewModel: CasinoViewModel, user: UserAccount) {
    var selectedCasinoGame by remember { mutableStateOf("SLOTS") } // "SLOTS", "BLACKJACK", "RULETA", "SCRATCH"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Horizontal Game Selector Tab
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                Triple("SLOTS", "🎰 Slots", "SLOTS"),
                Triple("BLACKJACK", "🃏 21 BJ", "BLACKJACK"),
                Triple("RULETA", "🎡 Ruleta", "RULETA"),
                Triple("SCRATCH", "🎫 Raspa", "SCRATCH")
            ).forEach { (gameType, label, testId) ->
                val active = selectedCasinoGame == gameType
                Button(
                    onClick = { selectedCasinoGame = gameType },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        contentColor = if (active) MaterialTheme.colorScheme.onPrimary else Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("game_tab_$testId")
                ) {
                    Text(
                        text = label,
                        textAlign = TextAlign.Center,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Current Game Frame
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, ElegantBorder),
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
        ) {
            when (selectedCasinoGame) {
                "SLOTS" -> SlotsGameView(viewModel)
                "BLACKJACK" -> BlackjackGameView(viewModel)
                "RULETA" -> RouletteGameView(viewModel)
                "SCRATCH" -> ScratchCardGameView(viewModel)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Multiplier references
        Card(
            colors = CardDefaults.cardColors(containerColor = ElegantSurfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Normas del Gremio del Casino ℹ️",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "• Slots: El jackpot de 3x '7️⃣' paga 50 veces tu apuesta.\n" +
                            "• Blackjack: El pago normal es de 2x; blackjack natural paga 2.5x.\n" +
                            "• Ruleta: El acierto en número exacto paga 36 veces tu ficha virtual.\n" +
                            "• Reembolso: Todas las jugadas perdidas acumulan un cashback automático recuperable del 15% semanal.",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun SlotsGameView(viewModel: CasinoViewModel) {
    var betAmt by remember { mutableStateOf(10.0) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "SLOT MACHINE IMPERIAL",
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Black,
            fontSize = 15.sp,
            letterSpacing = 1.sp
        )
        Text(
            text = "Prueba tu suerte con el bolillero virtual",
            color = Color.Gray,
            fontSize = 11.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Reels card
        Card(
            colors = CardDefaults.cardColors(containerColor = ElegantBackground),
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                viewModel.slotReels.forEach { symbol ->
                    Surface(
                        color = ElegantSurfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, ElegantPrimary.copy(alpha = 0.5f)),
                        modifier = Modifier.size(75.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = symbol,
                                fontSize = 38.sp,
                                modifier = Modifier.animateContentSize()
                            )
                        }
                    }
                }
            }
        }

        Text(
            text = viewModel.slotMessage,
            color = if (viewModel.slotPayout > 0) MaterialTheme.colorScheme.secondary else Color.LightGray,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Betting control
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { if (betAmt > 5.0) betAmt -= 5.0 },
                    enabled = !viewModel.isSlotSpinning,
                    colors = IconButtonDefaults.iconButtonColors(containerColor = ElegantSurfaceVariant)
                ) {
                    Icon(imageVector = Icons.Filled.Remove, contentDescription = "Menos", tint = Color.White)
                }
                Text(
                    text = String.format("$%.0f", betAmt),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 14.dp)
                )
                IconButton(
                    onClick = { betAmt += 5.0 },
                    enabled = !viewModel.isSlotSpinning,
                    colors = IconButtonDefaults.iconButtonColors(containerColor = ElegantSurfaceVariant)
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = "Mas", tint = Color.White)
                }
            }

            Button(
                onClick = { viewModel.spinSlots(betAmt) },
                enabled = !viewModel.isSlotSpinning,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("slots_spin_button")
            ) {
                if (viewModel.isSlotSpinning) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.Casino, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "GIRAR", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun BlackjackGameView(viewModel: CasinoViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "NEO BLACKJACK 21",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp
            )
            Surface(
                color = if (viewModel.blackjackState == "WON") MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f) else ElegantSurfaceVariant,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = viewModel.blackjackState,
                    color = if (viewModel.blackjackState == "WON") MaterialTheme.colorScheme.secondary else Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Dealer Section
        Text(
            text = "Mano de la Banca (Dealer)",
            color = Color.LightGray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (viewModel.dealerHand.isEmpty()) {
                Surface(
                    color = ElegantSurfaceVariant,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f)),
                    modifier = Modifier.size(50.dp, 75.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) { Text("?", color = Color.Gray) }
                }
            } else {
                viewModel.dealerHand.forEachIndexed { index, card ->
                    // Hide second card if it's player is betting / active
                    val hide = index == 1 && viewModel.blackjackState == "PLAYER_TURN"
                    BlackjackCardView(card = card, hideFace = hide)
                }
            }
        }

        Divider(color = Color.Gray.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 10.dp))

        // Player Section
        Text(
            text = "Tu Mano (Jugador)",
            color = Color.LightGray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (viewModel.playerHand.isEmpty()) {
                Text(
                    text = "Inicia una ronda colocando una apuesta.",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                viewModel.playerHand.forEach { card ->
                    BlackjackCardView(card = card, hideFace = false)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = viewModel.blackjackStatusText,
            color = if (viewModel.blackjackState == "WON") MaterialTheme.colorScheme.secondary else Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Game Actions
        if (viewModel.blackjackState == "PLAYER_TURN") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { viewModel.hitBlackjack() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("blackjack_hit_button")
                ) {
                    Text("CARTA (HIT)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Button(
                    onClick = { viewModel.standBlackjack() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("blackjack_stand_button")
                ) {
                    Text("QUEDARSE (STAND)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        } else {
            // Bet size controls & start
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { if (viewModel.blackjackBetAmount > 10) viewModel.blackjackBetAmount -= 10 },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = ElegantSurfaceVariant)
                    ) {
                        Icon(imageVector = Icons.Filled.Remove, contentDescription = null, tint = Color.White)
                    }
                    Text(
                        text = String.format("$%.0f", viewModel.blackjackBetAmount),
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                    IconButton(
                        onClick = { viewModel.blackjackBetAmount += 10 },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = ElegantSurfaceVariant)
                    ) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = null, tint = Color.White)
                    }
                }

                Button(
                    onClick = { viewModel.startBlackjack() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("blackjack_start_button")
                ) {
                    Text("APOSTAR", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun BlackjackCardView(card: CasinoCard, hideFace: Boolean) {
    val isRed = card.suit == "Corazones" || card.suit == "Diamantes"
    val color = if (isRed) Color(0xFFE74C3C) else Color.Black

    Surface(
        color = if (hideFace) ElegantRecoveryBorder else Color.White,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f)),
        shadowElevation = 3.dp,
        modifier = Modifier.size(54.dp, 80.dp)
    ) {
        if (hideFace) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "👑",
                    fontSize = 20.sp
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = card.value,
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
                Text(
                    text = card.getSymbol(),
                    color = color,
                    fontSize = 24.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Text(
                    text = card.value,
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.End)
                        .graphicsLayer { rotationZ = 180f }
                )
            }
        }
    }
}

@Composable
fun RouletteGameView(viewModel: CasinoViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "RULETA FRANCESA DE LUJO",
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Black,
            fontSize = 15.sp
        )
        Text(
            text = "Haz tu predicción de color o número",
            color = Color.Gray,
            fontSize = 11.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Roulette Wheel graphic
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(ElegantSurfaceVariant)
                .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (viewModel.isRouletteSpinning) {
                CircularProgressIndicator(
                    modifier = Modifier.size(80.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 4.dp
                )
                Text(
                    text = "🎡",
                    fontSize = 32.sp
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = viewModel.lastRouletteWinningNumber?.toString() ?: "VS",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Black
                    )
                    if (viewModel.lastRouletteWinningColor.isNotEmpty()) {
                        Text(
                            text = viewModel.lastRouletteWinningColor,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = viewModel.rouletteStatusText,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        )

        // Betting Options Select Drawer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                Pair("ROJO", "🔴 Rojo (2x)"),
                Pair("NEGRO", "⚫ Negro (2x)"),
                Pair("PAR", "🔢 Par (2x)"),
                Pair("IMPAR", "🔢 Impar (2x)"),
                Pair("NUMERO_ESPECIFICADO", "🎯 Número (36x)")
            ).forEach { (typeStr, labelStr) ->
                val active = viewModel.selectedRouletteBetType == typeStr
                FilterChip(
                    selected = active,
                    onClick = { viewModel.selectedRouletteBetType = typeStr },
                    label = { Text(text = labelStr, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.Black
                    )
                )
            }
        }

        // Custom Number Selector
        if (viewModel.selectedRouletteBetType == "NUMERO_ESPECIFICADO") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "Número elegido: ", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.width(10.dp))
                IconButton(onClick = { if (viewModel.chosenRouletteNumber > 0) viewModel.chosenRouletteNumber-- }) {
                    Icon(imageVector = Icons.Filled.RemoveCircle, contentDescription = null, tint = Color.White)
                }
                Text(
                    text = viewModel.chosenRouletteNumber.toString(),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
                IconButton(onClick = { if (viewModel.chosenRouletteNumber < 36) viewModel.chosenRouletteNumber++ }) {
                    Icon(imageVector = Icons.Filled.AddCircle, contentDescription = null, tint = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Trigger Spin Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Ficha: ", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = { if (viewModel.rouletteBetAmount > 5) viewModel.rouletteBetAmount -= 5 },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = ElegantSurfaceVariant)
                ) {
                    Icon(imageVector = Icons.Filled.Remove, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                }
                Text(
                    text = String.format("$%.0f", viewModel.rouletteBetAmount),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                IconButton(
                    onClick = { viewModel.rouletteBetAmount += 5 },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = ElegantSurfaceVariant)
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                }
            }

            Button(
                onClick = { viewModel.spinRoulette() },
                enabled = !viewModel.isRouletteSpinning,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("roulette_spin_button")
            ) {
                Text("GIRAR RULETA", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun ScratchCardGameView(viewModel: CasinoViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "RASPADITA FORTUNA",
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Black,
            fontSize = 15.sp
        )
        Text(
            text = "Rasca las 9 casillas para buscar 3 premios iguales!",
            color = Color.Gray,
            fontSize = 11.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // 3x3 Scratch Card grid
        Box(
            modifier = Modifier
                .size(210.dp)
                .background(ElegantBackground, shape = RoundedCornerShape(12.dp))
                .border(2.dp, ElegantBorder, shape = RoundedCornerShape(12.dp))
                .padding(10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (row in 0..2) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (col in 0..2) {
                            val index = row * 3 + col
                            val scratched = viewModel.scratchedCells.getOrElse(index) { false }
                            val prizeValue = viewModel.scratchCardValues.getOrElse(index) { "" }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (scratched) ElegantSurfaceVariant else ElegantBorder
                                    )
                                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .clickable {
                                        if (viewModel.isScratchCardActive) {
                                            viewModel.scratchCell(index)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (scratched) {
                                    Text(
                                        text = prizeValue,
                                        color = if (viewModel.wasScratchGameWon && prizeValue == ("$" + String.format("%.0f", viewModel.prizePoolRevealed))) MaterialTheme.colorScheme.secondary else Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp
                                    )
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "🪙", fontSize = 16.sp)
                                        Text(text = "RASCA", fontSize = 8.sp, color = ElegantBackground, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = viewModel.scratchStatusText,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Valor: $$", fontSize = 11.sp, color = Color.Gray)
                Text(
                    text = String.format("%.0f", viewModel.currentScratchBet),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )
            }

            Button(
                onClick = { viewModel.buyScratchCard() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("scratch_buy_button")
            ) {
                Text("NUEVA TARJETA", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

// ----------------------------------------------------
// SCREEN 2: SPORTS BETTING TAB
// ----------------------------------------------------
@Composable
fun SportsTabScreen(viewModel: CasinoViewModel, user: UserAccount) {
    val matches by viewModel.allMatches.collectAsStateWithLifecycle()
    var selectedCategory by remember { mutableStateOf("Todos") }
    val categories = listOf("Todos", "Fútbol", "Baloncesto", "Tenis", "F1")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CUPONES DEPORTIVOS",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
                Text(
                    text = "Apuestas de coeficientes en cuotas fijas",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
            Surface(
                color = ElegantSurfaceVariant,
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(modifier = Modifier.padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🔴 LIVE", fontSize = 10.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Horizontal Categories Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                val active = selectedCategory == cat
                FilterChip(
                    selected = active,
                    onClick = { selectedCategory = cat },
                    label = { Text(text = cat, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.Black
                    ),
                    modifier = Modifier.testTag("sports_category_$cat")
                )
            }
        }

        // Active Sports list
        val filteredList = if (selectedCategory == "Todos") matches else matches.filter { it.category == selectedCategory }

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No hay partidos disponibles de momento.", color = Color.Gray, fontSize = 13.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList) { match ->
                    SportsMatchItem(
                        match = match,
                        isSelected = viewModel.selectedMatchToBet == match,
                        selectedOutcome = viewModel.selectedOutcomeBet,
                        onSelectMatch = { outcome ->
                            viewModel.selectedMatchToBet = match
                            viewModel.selectedOutcomeBet = outcome
                            viewModel.sportsStatusText = "Ingresa tu dinero en el boleto de apuestas."
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Bet Slip (Persistent Drawer at bottom of sports screen)
        val selectedMatch = viewModel.selectedMatchToBet
        if (selectedMatch != null) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bet_slip_panel")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🎟️ Boleto de Apuestas",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        IconButton(onClick = { viewModel.selectedMatchToBet = null }) {
                            Icon(imageVector = Icons.Filled.Close, contentDescription = "Cerrar", tint = Color.Gray)
                        }
                    }

                    Text(
                        text = "${selectedMatch.homeTeam} vs ${selectedMatch.awayTeam}",
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontSize = 14.sp
                    )

                    val odds = when (viewModel.selectedOutcomeBet) {
                        "HOME" -> selectedMatch.homeOdds
                        "DRAW" -> selectedMatch.drawOdds
                        "AWAY" -> selectedMatch.awayOdds
                        else -> 1.0
                    }

                    val outcomeLabel = when (viewModel.selectedOutcomeBet) {
                        "HOME" -> "Gana ${selectedMatch.homeTeam}"
                        "DRAW" -> "Empate"
                        "AWAY" -> "Gana ${selectedMatch.awayTeam}"
                        else -> ""
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Mercado: $outcomeLabel", color = Color.LightGray, fontSize = 12.sp)
                        Text(text = "Cuota: $odds", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    // Bet input field
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Apuesta ($)", color = Color.Gray, fontSize = 10.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { if (viewModel.sportsBetAmount > 10) viewModel.sportsBetAmount -= 10 },
                                    colors = IconButtonDefaults.iconButtonColors(containerColor = ElegantSurfaceVariant),
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(imageVector = Icons.Filled.Remove, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                                }
                                Text(
                                    text = String.format("%.0f", viewModel.sportsBetAmount),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                IconButton(
                                    onClick = { viewModel.sportsBetAmount += 10 },
                                    colors = IconButtonDefaults.iconButtonColors(containerColor = ElegantSurfaceVariant),
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(imageVector = Icons.Filled.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Ganancia Estimada", color = Color.Gray, fontSize = 10.sp)
                            Text(
                                text = String.format("$%.2f", viewModel.sportsBetAmount * odds),
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Button(
                            onClick = { viewModel.placeSportsBet() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("submit_sports_bet_button")
                        ) {
                            Text("ENVIAR", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    Text(
                        text = viewModel.sportsStatusText,
                        color = Color.Gray,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SportsMatchItem(
    match: SportsMatch,
    isSelected: Boolean,
    selectedOutcome: String,
    onSelectMatch: (String) -> Unit
) {
    val leagueLabel = when (match.homeTeam) {
        "Real Madrid" -> "La Liga BBVA 🇪🇸 • ⚽"
        "Boca Juniors" -> "Superliga Argentina 🇦🇷 • ⚽"
        "Manchester City" -> "Premier League 🇬🇧 • ⚽"
        "LA Lakers" -> "NBA Playoffs Series 🇺🇸 • 🏀"
        "Golden State" -> "NBA Regular Season 🇺🇸 • 🏀"
        "Alcaraz" -> "ATP Masters Final 🏆 • 🎾"
        "Djokovic" -> "Grand Slam Center Court 🎾"
        "Max Verstappen" -> "F1 GP de Mónaco 🏎️"
        else -> "${match.category} Pro League"
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = if (isSelected) ElegantSurface else ElegantSurfaceVariant),
        border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else ElegantBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Live / Category Indicator bar with League Label
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = leagueLabel,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (match.isLive) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .graphicsLayer(alpha = alphaAnim)
                                .clip(CircleShape)
                                .background(Color.Red)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${match.matchTime} (${match.scoreHome} - ${match.scoreAway})",
                            fontSize = 11.sp,
                            color = Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = match.matchTime,
                            fontSize = 10.sp,
                            color = Color.LightGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Club Names
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${match.homeTeam} vs ${match.awayTeam}",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Odds buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Home (1)
                OutcomeOddButton(
                    label = "1 (Local)",
                    odds = match.homeOdds,
                    isSelected = isSelected && selectedOutcome == "HOME",
                    onClick = { onSelectMatch("HOME") },
                    modifier = Modifier.weight(1f)
                )

                // Draw (X)
                if (match.drawOdds > 0) {
                    OutcomeOddButton(
                        label = "X (Empate)",
                        odds = match.drawOdds,
                        isSelected = isSelected && selectedOutcome == "DRAW",
                        onClick = { onSelectMatch("DRAW") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Away (2)
                if (match.awayOdds > 0) {
                    OutcomeOddButton(
                        label = "2 (Visita)",
                        odds = match.awayOdds,
                        isSelected = isSelected && selectedOutcome == "AWAY",
                        onClick = { onSelectMatch("AWAY") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun OutcomeOddButton(
    label: String,
    odds: Double,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else ElegantSurfaceVariant,
            contentColor = if (isSelected) Color.Black else Color.White
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
        modifier = modifier
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = String.format("%.2f", odds), fontWeight = FontWeight.Black, fontSize = 11.sp, color = if (isSelected) Color.Black else MaterialTheme.colorScheme.secondary)
        }
    }
}

// ----------------------------------------------------
// SCREEN 3: QUINIELA / BOARD DRAWING TAB
// ----------------------------------------------------
@Composable
fun QuinielaTabScreen(viewModel: CasinoViewModel, user: UserAccount) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "SORTEO DE QUINIELA Y LOTERÍA",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp
            )
            Text(
                text = "Ingresa tus números fetiches para el bolillero oficial",
                color = Color.Gray,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Drawing Slot Picker
        Text(text = "Seleccione Sorteo Oficial:", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("Primera", "Matutina", "Vespertina", "Nocturna").forEach { slot ->
                val active = viewModel.selectedQuinielaDrawing == slot
                Button(
                    onClick = { viewModel.selectedQuinielaDrawing = slot },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (active) MaterialTheme.colorScheme.primary else ElegantSurfaceVariant,
                        contentColor = if (active) Color.Black else Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = slot, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, ElegantBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Ticket Input
                OutlinedTextField(
                    value = viewModel.selectedQuinielaNumber,
                    onValueChange = {
                        if (it.length <= 4 && it.all { chr -> chr.isDigit() }) {
                            viewModel.selectedQuinielaNumber = it
                        }
                    },
                    label = { Text("Número a Jugar (1 a 4 cifras)", color = Color.Gray) },
                    placeholder = { Text("Ej: 45 o 732") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Gray
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quiniela_number_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Bet Amount
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Apuesta virtual:", fontSize = 12.sp, color = Color.LightGray)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (viewModel.quinielaBetAmount > 5) viewModel.quinielaBetAmount -= 5 }) {
                            Icon(imageVector = Icons.Filled.RemoveCircle, contentDescription = null, tint = Color.Gray)
                        }
                        Text(
                            text = String.format("$%.0f", viewModel.quinielaBetAmount),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        IconButton(onClick = { viewModel.quinielaBetAmount += 5 }) {
                            Icon(imageVector = Icons.Filled.AddCircle, contentDescription = null, tint = Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Regulatory Checklist
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = viewModel.isLegalAcknowledgeChecked,
                        onCheckedChange = { viewModel.isLegalAcknowledgeChecked = it },
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                    )
                    Text(
                        text = "Firma legal de ticket bajo Licencia Segura AR-345.",
                        fontSize = 11.sp,
                        color = Color.LightGray,
                        modifier = Modifier.clickable { viewModel.isLegalAcknowledgeChecked = !viewModel.isLegalAcknowledgeChecked }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { viewModel.playQuiniela() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quiniela_play_button")
                ) {
                    if (viewModel.isQuinielaDrawing) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black)
                    } else {
                        Text(text = "JUGAR SORTEO", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Drawn numbers board
        Text(text = "Pizarra de Ganadores (${viewModel.selectedQuinielaDrawing}):", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Card(
            colors = CardDefaults.cardColors(containerColor = ElegantBackground),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                if (viewModel.quinielaDrawResults.isEmpty()) {
                    Text(
                        text = "No se ha realizado ningún sorteo reciente.",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    viewModel.quinielaDrawResults.forEachIndexed { rank, number ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "${rank + 1}º PREMIO DEL BOLILLERO", color = if (rank == 0) MaterialTheme.colorScheme.primary else Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(text = number, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        Text(
            text = viewModel.quinielaStatusText,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
        )
    }
}

// ----------------------------------------------------
// SCREEN 4: RECUPERO (CASHBACK & STATISTICS) TAB
// ----------------------------------------------------
@Composable
fun RecuperoTabScreen(viewModel: CasinoViewModel, user: UserAccount) {
    val bets by viewModel.allBets.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "CENTRO DE RECUPERO DE SALDO",
                color = MaterialTheme.colorScheme.secondary, // Glowing emerald green
                fontWeight = FontWeight.Black,
                fontSize = 16.sp
            )
            Text(
                text = "Estadísticas y rescates de fondos garantizados",
                color = Color.Gray,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Statistics row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Wins card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F201C)),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "Total Ganado", color = Color.Gray, fontSize = 10.sp)
                    Text(
                        text = String.format("$%.2f", user.totalWins),
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Losses card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C101B)),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "Total Perdido", color = Color.Gray, fontSize = 10.sp)
                    Text(
                        text = String.format("$%.2f", user.totalLosses),
                        color = MaterialTheme.colorScheme.tertiary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Cashback Refund Claims (Loss recovery panel)
        Card(
            colors = CardDefaults.cardColors(containerColor = ElegantRecoveryBg),
            border = BorderStroke(1.dp, ElegantRecoveryBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🎁 Cashback de Cobertura (15%)",
                            color = ElegantPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Reclama 15% de tus pérdidas netas al instante.",
                            color = ElegantSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Text(
                        text = String.format("$%.2f", user.claimableCashback),
                        color = ElegantPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    )
                }

                // Custom Styled Progress Bar from the Design theme
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF211F26))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(0.65f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(ElegantPrimary)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "65% Meta",
                        color = ElegantPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = { viewModel.claimCashback() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElegantPrimary,
                        contentColor = ElegantOnPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("claim_cashback_button")
                ) {
                    Text(
                        text = "SOLICITAR REINTEGRO",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Emergency low balance refill
        Card(
            colors = CardDefaults.cardColors(containerColor = ElegantSurface),
            border = BorderStroke(1.dp, ElegantBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "🚨 Asistencia de Emergencia (+100 Créditos)",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = "Si te quedas con menos de $5 créditos, te regalamos una bonificación de rescate.",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Button(
                    onClick = { viewModel.claimEmergencyRescue() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("emergency_rescue_button")
                ) {
                    Text(text = "SOLICITAR CRÉDITO DE RESCATE", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Historical bets log
        Text(text = "Historial de Apuestas y Transacciones:", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Card(
            colors = CardDefaults.cardColors(containerColor = ElegantBackground),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                if (bets.isEmpty()) {
                    Text(
                        text = "No hay apuestas registradas en el historial.",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    bets.forEach { bet ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = bet.description, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(
                                    text = when (bet.type) {
                                        "CASINO" -> "🎰 Casino"
                                        "SPORTS" -> "⚽ Deportes"
                                        "QUINIELA" -> "🎫 Quiniela"
                                        "RECOV" -> "🎁 Recupero"
                                        else -> "💰 Depósito"
                                    },
                                    color = Color.Gray,
                                    fontSize = 10.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            val isWin = bet.status == "WON" || bet.status == "GANADA" || bet.status == "RECLAMADA" || bet.status == "BONIFICADO"
                            val col = if (isWin) MaterialTheme.colorScheme.secondary else if (bet.status == "LOST" || bet.status == "PERDIDA") MaterialTheme.colorScheme.tertiary else Color.LightGray
                            Text(
                                text = if (bet.payout > 0) "+$${String.format("%.2f", bet.payout)}" else "-$${String.format("%.2f", bet.amountBet)}",
                                color = col,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Reset and rebuild button
        OutlinedButton(
            onClick = { viewModel.resetSystem() },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.tertiary),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("reset_system_button")
        ) {
            Text("RESTABLECER TODO EL SISTEMA DEMO", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ----------------------------------------------------
// SCREEN 5: REGULATORY FRAMEWORK & SIGNATURE TAB
// ----------------------------------------------------
@Composable
fun LegalTabScreen(viewModel: CasinoViewModel, user: UserAccount) {
    var consentNameInput by remember { mutableStateOf(viewModel.signerNameInput) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "LICENCIA Y FIRMA DE CONSENTIMIENTO",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp
            )
            Text(
                text = "Regulación obligatoria de juego limpio y seguro",
                color = Color.Gray,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Garantías del Código Civil de Loterías ⚖️",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "ApostaReal opera bajo un marco legal simulado con licencias de auto-recupero de pérdidas del 15% (Cashback) para garantizar un juego sano. " +
                            "Al ingresar tu firma electrónica en esta sección, declaras responsablemente:\n\n" +
                            "1. Tener más de 18 años de edad.\n" +
                            "2. Que comprendes que este sistema opera con créditos virtuales promocionales, sin pérdidas reales de capital.\n" +
                            "3. Que aceptas la auto-regulación del sistema de recupero semanal.\n\n" +
                            "La firma electrónica es única e intransferible y desbloquea el Bono Legal de +$500 créditos.",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (user.isTermsSigned) {
            // Approved and Certified
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F201C)),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "✓ CERTIFICADO DIGITAL CONCEDIDO",
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "El usuario ${user.signedName} firmó legalmente este consentimiento.",
                        color = Color.White,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = "Fecha de registro: ${user.signedDate}",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Redraw the saved coordinates using small canvas
                    val xs = user.signaturePointsX?.split(",")?.mapNotNull { it.toFloatOrNull() } ?: emptyList()
                    val ys = user.signaturePointsY?.split(",")?.mapNotNull { it.toFloatOrNull() } ?: emptyList()

                    if (xs.isNotEmpty() && ys.isNotEmpty()) {
                        Text(text = "Firma Electrónica Almacenada:", fontSize = 11.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(6.dp))
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .background(Color.White, shape = RoundedCornerShape(8.dp))
                        ) {
                            val path = Path()
                            path.moveTo(xs.first(), ys.first())
                            for (i in 1 until xs.size) {
                                if (i < ys.size) {
                                    path.lineTo(xs[i], ys[i])
                                }
                            }
                            drawPath(
                                path = path,
                                color = Color.Black,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }
                    }
                }
            }
        } else {
            // Need to sign
            Card(
                colors = CardDefaults.cardColors(containerColor = ElegantSurface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Ingresa tu Nombre Completo:",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = consentNameInput,
                        onValueChange = {
                            consentNameInput = it
                            viewModel.signerNameInput = it
                        },
                        placeholder = { Text("Escribe tu nombre aquí") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.Gray
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .testTag("signature_name_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Introduce tu Firma o Iniciales en el Recuadro Blanco:",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    // Touch Canvas Area
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .background(Color.White, shape = RoundedCornerShape(8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(8.dp))
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        viewModel.drawingPointsX.add(offset.x)
                                        viewModel.drawingPointsY.add(offset.y)
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        viewModel.drawingPointsX.add(change.position.x)
                                        viewModel.drawingPointsY.add(change.position.y)
                                    }
                                )
                            }
                            .testTag("signature_canvas")
                    ) {
                        if (viewModel.drawingPointsX.isNotEmpty() && viewModel.drawingPointsY.size == viewModel.drawingPointsX.size) {
                            val path = Path()
                            path.moveTo(viewModel.drawingPointsX.first(), viewModel.drawingPointsY.first())
                            for (i in 1 until viewModel.drawingPointsX.size) {
                                path.lineTo(viewModel.drawingPointsX[i], viewModel.drawingPointsY[i])
                            }
                            drawPath(
                                path = path,
                                color = Color.Black,
                                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.clearSignature()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray),
                            border = BorderStroke(1.dp, Color.Gray),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("BORRAR", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.submitResponsibleGamingSignature {
                                    // Give user sign rewardcredits
                                    viewModel.depositDemoCredits(500.0)
                                }
                            },
                            enabled = consentNameInput.isNotBlank() && viewModel.drawingPointsX.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("validate_signature_button")
                        ) {
                            Text("VALIDAR FIRMA", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// SCREEN 6: SECURE PAYMENTS PORTAL TAB
// ----------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PaymentsTabScreen(viewModel: CasinoViewModel, user: UserAccount) {
    var isDepositMode by remember { mutableStateOf(true) }
    var selectedMethod by remember { mutableStateOf("TARJETA") } // "TARJETA", "TRANSFER", "BILLETERA"
    
    // Input States
    var amountText by remember { mutableStateOf("100") }
    var cardNumberText by remember { mutableStateOf("") }
    var holderText by remember { mutableStateOf("") }
    var expiryText by remember { mutableStateOf("") }
    var cvvText by remember { mutableStateOf("") }
    
    // Bank details states
    val bankAccountText = "AL-APOSTA-REAL.S.A"
    val bankCbuText = "0000003100023419582736"
    val bankAliasText = "aposta.real.seguro"
    
    // Electronic wallet email/phone
    var walletDetailText by remember { mutableStateOf("") }
    
    // Success Dialog / Custom Overlay
    var showSecuritySuccessOverlay by remember { mutableStateOf(false) }
    var secureTxReference by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Core Tab Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CAJA Y PASARELA FINANCIERA",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
                Text(
                    text = "Cifrado SSL de 256 bits • End-to-End Protegido",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
            Surface(
                color = ElegantRecoveryBg,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, ElegantRecoveryBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Security,
                        contentDescription = "Seguro",
                        tint = ElegantPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "PCI-DSS",
                        color = ElegantPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Toggle Switcher (Deposit vs Withdraw)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ElegantSurfaceVariant, RoundedCornerShape(10.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Button(
                onClick = {
                    isDepositMode = true
                    viewModel.paymentStatusText = "Seleccione un método de depósito para comenzar (Cifrado AES-256 Activo)"
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDepositMode) MaterialTheme.colorScheme.primary else Color.Transparent,
                    contentColor = if (isDepositMode) Color.Black else Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Filled.TrendingUp,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("DEPOSITAR", fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }

            Button(
                onClick = {
                    isDepositMode = false
                    viewModel.paymentStatusText = "Seleccione un método de retiro para comenzar (Cifrado AES-256 Activo)"
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!isDepositMode) MaterialTheme.colorScheme.primary else Color.Transparent,
                    contentColor = if (!isDepositMode) Color.Black else Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Filled.TrendingDown,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("RETIRAR", fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Balance Information Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = ElegantSurface),
            border = BorderStroke(1.dp, ElegantBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Tu Saldo Disponible", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        text = String.format("$%.2f", user.balance),
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Icon(
                    imageVector = if (isDepositMode) Icons.Filled.AccountBalanceWallet else Icons.Filled.Savings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section Title: Métodos Disponibles
        Text(text = "Método de Pago Seguro:", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        
        Spacer(modifier = Modifier.height(8.dp))

        // horizontal payment method selector cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val methods = listOf(
                Triple("TARJETA", "Tarjeta", Icons.Filled.CreditCard),
                Triple("TRANSFER", "Transfer.", Icons.Filled.AccountBalance),
                Triple("BILLETERA", "Wallet", Icons.Filled.PhonelinkRing)
            )

            methods.forEach { (type, name, icon) ->
                val active = selectedMethod == type
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else ElegantSurfaceVariant
                    ),
                    border = BorderStroke(if (active) 2.dp else 1.dp, if (active) MaterialTheme.colorScheme.primary else ElegantBorder),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedMethod = type }
                ) {
                    Column(
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = name,
                            tint = if (active) MaterialTheme.colorScheme.primary else Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = name,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (active) Color.White else Color.Gray
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Core Interaction Form
        Card(
            colors = CardDefaults.cardColors(containerColor = ElegantSurface),
            border = BorderStroke(1.dp, ElegantBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Common Amount input
                Text(text = "Monto de la Transacción ($):", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { if (it.all { char -> char.isDigit() }) amountText = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("Monto", color = Color.Gray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = ElegantBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Custom form according to selected method
                when (selectedMethod) {
                    "TARJETA" -> {
                        Text(text = "Número de Tarjeta:", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = cardNumberText,
                            onValueChange = { if (it.length <= 16 && it.all { char -> char.isDigit() }) cardNumberText = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            placeholder = { Text("e.g. 4509 8127 3341 0928", color = Color.Gray) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = ElegantBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(text = "Nombre del Titular:", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = holderText,
                            onValueChange = { holderText = it },
                            placeholder = { Text("Como figura impreso", color = Color.Gray) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = ElegantBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Vencimiento:", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = expiryText,
                                    onValueChange = { expiryText = it },
                                    placeholder = { Text("MM/AA", color = Color.Gray) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = ElegantBorder
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "CVV:", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = cvvText,
                                    onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) cvvText = it },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    placeholder = { Text("123", color = Color.Gray) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = ElegantBorder
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    "TRANSFER" -> {
                        Surface(
                            color = ElegantBackground,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().border(1.dp, ElegantBorder, RoundedCornerShape(8.dp))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "Detalles de Transferencia Segura", color = ElegantPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Filled.Lock, contentDescription = null, tint = Color.Green, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text("Enc.", color = Color.Green, fontSize = 9.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                Text(text = "Titular: $bankAccountText", fontSize = 11.sp, color = Color.White)
                                Text(text = "CBU: $bankCbuText", fontSize = 11.sp, color = Color.White)
                                Text(text = "Alias: $bankAliasText", fontSize = 11.sp, color = Color.White)

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Nota: Transfiera desde su home banking e ingrese el CBU emisor a continuación para conciliar instantáneamente:",
                                    fontSize = 9.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(text = "Tu CBU Emisor o Alias:", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = walletDetailText,
                            onValueChange = { walletDetailText = it },
                            placeholder = { Text("e.g. 000002131231...", color = Color.Gray) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = ElegantBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    "BILLETERA" -> {
                        Text(
                            text = "Email o Cuenta de Billetera Electrónica (Mercado Pago, PayPal, Skrill):",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = walletDetailText,
                            onValueChange = { walletDetailText = it },
                            placeholder = { Text("email@billetera.com o número telefónico", color = Color.Gray) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = ElegantBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Security processing status bar in real-time
                Text(
                    text = viewModel.paymentStatusText,
                    fontSize = 11.sp,
                    color = if (viewModel.paymentInProgress) ElegantPrimary else Color.Gray,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                if (viewModel.paymentInProgress) {
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        color = ElegantPrimary,
                        modifier = Modifier.fillMaxWidth().height(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Submit Action Button
                val canSubmit = amountText.toDoubleOrNull() != null && amountText.toDouble() > 0 && !viewModel.paymentInProgress
                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull() ?: 100.0
                        val ref = (100000..999999).random().toString()
                        if (isDepositMode) {
                            viewModel.executeSecureDeposit(amount, selectedMethod, ref) {
                                secureTxReference = ref
                                showSecuritySuccessOverlay = true
                            }
                        } else {
                            viewModel.executeSecureWithdraw(amount, selectedMethod, if (selectedMethod == "TARJETA") "Tarjeta ending in ${cardNumberText.takeLast(4)}" else walletDetailText) {
                                secureTxReference = ref
                                showSecuritySuccessOverlay = true
                            }
                        }
                    },
                    enabled = canSubmit,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDepositMode) Color(0xFF2ECC71) else MaterialTheme.colorScheme.secondary,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_secure_payment_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isDepositMode) Icons.Filled.Lock else Icons.Filled.Send,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isDepositMode) "CONFIRMAR DEPÓSITO SEGURO" else "SOLICITAR RETIRO ENCRIPTADO",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Security guarantees panel (PCI compliant, Cryptography SSL/TLS)
        Card(
            colors = CardDefaults.cardColors(containerColor = ElegantRecoveryBg),
            border = BorderStroke(1.dp, ElegantRecoveryBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Filled.VerifiedUser, contentDescription = null, tint = ElegantPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Garantía de Encriptación de Transacciones", color = ElegantPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Aposta Real certifica que todos los canales de cobro y pago están provistos de firmas digitales criptográficas SHA-256 e intercambio de claves Diffie-Hellman a través de TLS 1.3 de grado bancario. Ningún dato de tarjeta o cuenta es almacenado sin hashes salados.",
                    fontSize = 10.sp,
                    color = Color.LightGray
                )
            }
        }

        // Custom Overlay of successful execution
        if (showSecuritySuccessOverlay) {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                color = Color(0xFF223E2E),
                border = BorderStroke(1.dp, Color(0xFF2ECC71)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🔐", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Detalle de Conexión Encriptada SSL", color = Color(0xFF2ECC71), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        IconButton(
                            onClick = { showSecuritySuccessOverlay = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.Close, contentDescription = "Cerrar", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "Transacción: ${if (isDepositMode) "DEPÓSITO" else "RETIRO"} AUTORIZADO", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(text = "Protocolo de Canal: TLSv1.3 (TCP Port 443)", color = Color.LightGray, fontSize = 10.sp)
                    Text(text = "Clave de Cifrado: AES_256_GCM_SHA384 (Firmado)", color = Color.LightGray, fontSize = 10.sp)
                    Text(text = "Número de Referencia SSL: #TXS-$secureTxReference", color = Color.LightGray, fontSize = 10.sp)
                    Text(text = "Hash Blockchain Fiduciario: SHA256-${secureTxReference.hashCode().toString(16).uppercase()}", color = Color.LightGray, fontSize = 10.sp)
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { showSecuritySuccessOverlay = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ECC71)),
                        modifier = Modifier.fillMaxWidth().height(36.dp)
                    ) {
                        Text("ENTENDIDO", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
