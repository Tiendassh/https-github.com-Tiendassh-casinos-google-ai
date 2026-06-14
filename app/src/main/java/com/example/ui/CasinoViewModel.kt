package com.example.ui

import android.app.Application
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Card represents a casino card
data class CasinoCard(val suit: String, val value: String, val rank: Int) {
    fun scoreValue(currentTotal: Int): Int {
        return when (value) {
            "A" -> if (currentTotal + 11 <= 21) 11 else 1
            "K", "Q", "J" -> 10
            else -> value.toInt()
        }
    }
    fun getSymbol(): String {
        return when (suit) {
            "Corazones" -> "♥"
            "Diamantes" -> "♦"
            "Tréboles" -> "♣"
            "Espadas" -> "♠"
            else -> ""
        }
    }
}

class CasinoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CasinoRepository(application)
    
    // Play sounds using built-in Retro Tone generator helper
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 85)
        } catch (e: Exception) {
            // Ignore if audio not available
        }
        viewModelScope.launch {
            repository.preseedMatchesIfEmpty()
        }
        // Start continuous ticker for Live Sports simulation
        viewModelScope.launch {
            while (true) {
                delay(12000)
                repository.simulateLiveMatchesTick()
            }
        }
    }

    private fun playSound(type: String) {
        viewModelScope.launch {
            try {
                when (type) {
                    "click" -> toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
                    "lose" -> {
                        toneGenerator?.startTone(ToneGenerator.TONE_CDMA_PIP, 120)
                        delay(120)
                        toneGenerator?.startTone(ToneGenerator.TONE_CDMA_PIP, 200)
                    }
                    "win" -> {
                        toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ONE_MIN_BEEP, 100)
                        delay(100)
                        toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ONE_MIN_BEEP, 150)
                        delay(150)
                        toneGenerator?.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 250)
                    }
                    "jackpot" -> {
                        val tones = listOf(
                            ToneGenerator.TONE_PROP_BEEP,
                            ToneGenerator.TONE_CDMA_HIGH_L,
                            ToneGenerator.TONE_CDMA_HIGH_PBX_L,
                            ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE
                        )
                        for (t in tones) {
                            toneGenerator?.startTone(t, 120)
                            delay(120)
                        }
                    }
                    "spin" -> {
                        toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 60)
                    }
                }
            } catch (e: Exception) {
                // Ignore audio errors
            }
        }
    }

    val userAccount: StateFlow<UserAccount?> = repository.userAccount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allBets: StateFlow<List<BetHistory>> = repository.allBets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMatches: StateFlow<List<SportsMatch>> = repository.allMatches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // --- SIGNATURE SECTION ---
    var drawingPointsX = mutableListOf<Float>()
    var drawingPointsY = mutableListOf<Float>()
    var signerNameInput by mutableStateOf("")

    fun clearSignature() {
        drawingPointsX.clear()
        drawingPointsY.clear()
    }

    fun submitResponsibleGamingSignature(onComplete: () -> Unit) {
        if (signerNameInput.isBlank()) return
        val xString = drawingPointsX.joinToString(",") { String.format("%.1f", it) }
        val yString = drawingPointsY.joinToString(",") { String.format("%.1f", it) }

        viewModelScope.launch {
            repository.signTerms(signerNameInput, xString, yString)
            playSound("win")
            onComplete()
        }
    }


    // --- DEPOSIT demo credits ---
    fun depositDemoCredits(amount: Double) {
        viewModelScope.launch {
            playSound("win")
            repository.depositCredits(amount)
        }
    }

    // --- RECOVERY CASHBACK CLAIM ---
    var feedbackMessage by mutableStateOf("")
    fun claimCashback() {
        viewModelScope.launch {
            val success = repository.claimCashback()
            if (success) {
                playSound("win")
                feedbackMessage = "¡Recupero del 15% acreditado exitosamente!"
            } else {
                playSound("lose")
                feedbackMessage = "No tienes pérdidas acumuladas elegibles para el recupero de saldo."
            }
        }
    }

    fun claimEmergencyRescue() {
        viewModelScope.launch {
            val refilled = repository.getEmergencyCredits()
            if (refilled) {
                playSound("win")
                feedbackMessage = "¡Recupero de emergencia activado! Se te han recargado $100 créditos virtuales."
            } else {
                playSound("lose")
                feedbackMessage = "Para solicitar la asistencia de emergencia, tu balance debe ser menor a $5 créditos."
            }
        }
    }


    // --- SLOT MACHINE GAME STATE ---
    val slotSymbols = listOf("🍒", "🍋", "🍊", "🍇", "🔔", "💎", "7️⃣")
    var slotReels by mutableStateOf(listOf("🍒", "🍒", "🍒"))
    var isSlotSpinning by mutableStateOf(false)
    var slotPayout by mutableStateOf(0.0)
    var slotMessage by mutableStateOf("Elige tu apuesta y gira los carretes")

    fun spinSlots(betAmount: Double) {
        if (isSlotSpinning) return
        val currentBalance = userAccount.value?.balance ?: 0.0
        if (currentBalance < betAmount) {
            slotMessage = "Balance insuficiente."
            playSound("lose")
            return
        }

        viewModelScope.launch {
            isSlotSpinning = true
            slotMessage = "Girando..."

            // Animation simulation
            for (i in 1..8) {
                slotReels = listOf(slotSymbols.random(), slotSymbols.random(), slotSymbols.random())
                playSound("spin")
                delay(120)
            }

            // Final outcome
            val finalReels = listOf(
                slotSymbols.random(),
                slotSymbols.random(),
                slotSymbols.random()
            )
            slotReels = finalReels

            repository.processBet("CASINO", "Tragamonedas Bet: $$betAmount", betAmount) {
                val s1 = finalReels[0]
                val s2 = finalReels[1]
                val s3 = finalReels[2]

                var multiplier = 0.0
                if (s1 == s2 && s2 == s3) {
                    multiplier = when (s1) {
                        "7️⃣" -> 50.0
                        "💎" -> 35.0
                        "🔔" -> 20.0
                        "🍒" -> 15.0
                        else -> 10.0
                    }
                } else if (s1 == s2 || s2 == s3 || s1 == s3) {
                    val matchingSymbol = if (s1 == s2) s1 else s3
                    multiplier = if (matchingSymbol == "🍒") 3.0 else 2.0
                }

                val finalPayout = betAmount * multiplier
                Pair(multiplier > 0, finalPayout)
            }.also { (won, payoutAmt) ->
                isSlotSpinning = false
                slotPayout = payoutAmt
                if (won) {
                    playSound(if (payoutAmt >= betAmount * 15.0) "jackpot" else "win")
                    slotMessage = "¡GANASTE! Pagando $$payoutAmt (${payoutAmt / betAmount}x)"
                } else {
                    playSound("lose")
                    slotMessage = "Sigue intentando, ¡la suerte está cerca!"
                }
            }
        }
    }


    // --- BLACKJACK GAME STATE ---
    var blackjackState by mutableStateOf("BETTING") // "BETTING", "PLAYER_TURN", "DEALER_TURN", "WON", "LOST", "PUSH"
    var playerHand by mutableStateOf(emptyList<CasinoCard>())
    var dealerHand by mutableStateOf(emptyList<CasinoCard>())
    var blackjackBetAmount by mutableStateOf(20.0)
    var blackjackStatusText by mutableStateOf("Coloca tu apuesta para iniciar")

    private fun generateDeck(): List<CasinoCard> {
        val suits = listOf("Corazones", "Diamantes", "Tréboles", "Espadas")
        val values = listOf(
            Pair("2", 2), Pair("3", 3), Pair("4", 4), Pair("5", 5), Pair("6", 6),
            Pair("7", 7), Pair("8", 8), Pair("9", 9), Pair("10", 10),
            Pair("J", 10), Pair("Q", 10), Pair("K", 10), Pair("A", 11)
        )
        val deck = mutableListOf<CasinoCard>()
        for (suit in suits) {
            for (v in values) {
                deck.add(CasinoCard(suit, v.first, v.second))
            }
        }
        deck.shuffle()
        return deck
    }

    private var activeDeck = mutableListOf<CasinoCard>()

    fun startBlackjack() {
        val currentBalance = userAccount.value?.balance ?: 0.0
        if (currentBalance < blackjackBetAmount) {
            blackjackStatusText = "Balance insuficiente para esta apuesta."
            playSound("lose")
            return
        }

        viewModelScope.launch {
            // Setup local states
            activeDeck = generateDeck().toMutableList()
            val p1 = activeDeck.removeAt(0)
            val d1 = activeDeck.removeAt(0)
            val p2 = activeDeck.removeAt(0)
            val d2 = activeDeck.removeAt(0)

            playerHand = listOf(p1, p2)
            dealerHand = listOf(d1, d2)

            blackjackState = "PLAYER_TURN"
            blackjackStatusText = "Toma carta (HIT) o quédate (STAND)"
            playSound("click")

            // Check natural blackjack
            val pScore = calculateScore(playerHand)
            if (pScore == 21) {
                standBlackjack()
            }
        }
    }

    fun hitBlackjack() {
        if (blackjackState != "PLAYER_TURN") return
        viewModelScope.launch {
            playSound("spin")
            val card = activeDeck.removeAt(0)
            playerHand = playerHand + card

            val pScore = calculateScore(playerHand)
            if (pScore > 21) {
                // Player busted instantly
                resolveBlackjackOutcome(isBusted = true)
            } else if (pScore == 21) {
                // Auto stand
                standBlackjack()
            }
        }
    }

    fun standBlackjack() {
        if (blackjackState != "PLAYER_TURN") return
        blackjackState = "DEALER_TURN"
        blackjackStatusText = "Turno de la banca..."

        viewModelScope.launch {
            // Dealer must hit on < 17
            while (calculateScore(dealerHand) < 17) {
                delay(800)
                playSound("spin")
                val card = activeDeck.removeAt(0)
                dealerHand = dealerHand + card
            }
            delay(500)
            resolveBlackjackOutcome(isBusted = false)
        }
    }

    private fun calculateScore(cards: List<CasinoCard>): Int {
        var score = 0
        var acesCount = 0
        for (card in cards) {
            if (card.value == "A") {
                acesCount++
            } else {
                score += card.rank
            }
        }
        for (i in 0 until acesCount) {
            score += if (score + 11 <= 21) 11 else 1
        }
        return score
    }

    private suspend fun resolveBlackjackOutcome(isBusted: Boolean) {
        val pScore = calculateScore(playerHand)
        val dScore = calculateScore(dealerHand)

        repository.processBet("CASINO", "Blackjack Bet: $$blackjackBetAmount", blackjackBetAmount) {
            val won: Boolean
            val payout: Double

            if (isBusted) {
                won = false
                payout = 0.0
                blackjackState = "LOST"
                blackjackStatusText = "¡Has superado 21! Perdiste $$blackjackBetAmount"
            } else if (dScore > 21) {
                won = true
                payout = blackjackBetAmount * 2.0
                blackjackState = "WON"
                blackjackStatusText = "¡La casa supera 21! Ganaste $$payout"
            } else if (pScore > dScore) {
                won = true
                // paying 1.5x for natural blackjack (2 cards 21), or 2x default
                val isNatural = playerHand.size == 2 && pScore == 21
                payout = if (isNatural) blackjackBetAmount * 2.5 else blackjackBetAmount * 2.0
                blackjackState = "WON"
                blackjackStatusText = if (isNatural) "¡BLACKJACK NATURAL! Ganaste $$payout" else "¡Venciste a la casa! Ganaste $$payout"
            } else if (pScore < dScore) {
                won = false
                payout = 0.0
                blackjackState = "LOST"
                blackjackStatusText = "Te venció la casa ($pScore vs $dScore). Perdiste $$blackjackBetAmount"
            } else {
                // Push (Empate)
                won = false
                payout = blackjackBetAmount // return stake
                blackjackState = "PUSH"
                blackjackStatusText = "Empate ($pScore vs $dScore). Recuperas tu apuesta"
            }
            Pair(won || (blackjackState == "PUSH"), payout)
        }.also { (didWin, amountReward) ->
            if (blackjackState == "WON") {
                playSound("win")
            } else if (blackjackState == "LOST") {
                playSound("lose")
            } else {
                playSound("click")
            }
        }
    }


    // --- ROULETTE GAME STATE ---
    var selectedRouletteBetType by mutableStateOf("ROJO") // "ROJO", "NEGRO", "PAR", "IMPAR", "NUMERO_ESPECIFICADO"
    var chosenRouletteNumber by mutableStateOf(7) // default number bet
    var rouletteStatusText by mutableStateOf("Coloca tu ficha en color o número")
    var lastRouletteWinningNumber by mutableStateOf<Int?>(null)
    var lastRouletteWinningColor by mutableStateOf("")
    var isRouletteSpinning by mutableStateOf(false)
    var rouletteBetAmount by mutableStateOf(10.0)

    fun spinRoulette() {
        if (isRouletteSpinning) return
        val currentBalance = userAccount.value?.balance ?: 0.0
        if (currentBalance < rouletteBetAmount) {
            rouletteStatusText = "Balance de créditos insuficiente"
            playSound("lose")
            return
        }

        viewModelScope.launch {
            isRouletteSpinning = true
            rouletteStatusText = "La ruleta está girando..."

            // spin animation
            for (i in 1..10) {
                lastRouletteWinningNumber = (0..36).random()
                playSound("spin")
                delay(120)
            }

            val finalNumber = (0..36).random()
            lastRouletteWinningNumber = finalNumber

            val redNumbers = setOf(1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36)
            val isRed = redNumbers.contains(finalNumber)
            val finalColor = if (finalNumber == 0) "VERDE (Cero)" else if (isRed) "ROJO" else "NEGRO"
            lastRouletteWinningColor = finalColor

            repository.processBet("CASINO", "Ruleta Bet: $$rouletteBetAmount en $selectedRouletteBetType", rouletteBetAmount) {
                var won = false
                var payoutOdds = 0.0

                when (selectedRouletteBetType) {
                    "ROJO" -> {
                        if (finalColor == "ROJO") {
                            won = true
                            payoutOdds = 2.0
                        }
                    }
                    "NEGRO" -> {
                        if (finalColor == "NEGRO") {
                            won = true
                            payoutOdds = 2.0
                        }
                    }
                    "PAR" -> {
                        if (finalNumber != 0 && finalNumber % 2 == 0) {
                            won = true
                            payoutOdds = 2.0
                        }
                    }
                    "IMPAR" -> {
                        if (finalNumber % 2 != 0) {
                            won = true
                            payoutOdds = 2.0
                        }
                    }
                    "NUMERO_ESPECIFICADO" -> {
                        if (finalNumber == chosenRouletteNumber) {
                            won = true
                            payoutOdds = 36.0
                        }
                    }
                }

                val reward = rouletteBetAmount * payoutOdds
                Pair(won, reward)
            }.also { (didWin, payoutAmt) ->
                isRouletteSpinning = false
                if (didWin) {
                    playSound("win")
                    rouletteStatusText = "¡Número ganador: $finalNumber ($finalColor)! Ganaste $$payoutAmt"
                } else {
                    playSound("lose")
                    rouletteStatusText = "Salió: $finalNumber ($finalColor). Tu apuesta no ha ganado."
                }
            }
        }
    }


    // --- QUINIELA / BOARD GAME STATE ---
    var selectedQuinielaNumber by mutableStateOf("") // 2 digits (e.g. "45") or 3/4 digits
    var selectedQuinielaDrawing by mutableStateOf("Matutina") // "La Previa", "Primera", "Matutina", "Vespertina", "Nocturna"
    var quinielaBetAmount by mutableStateOf(10.0)
    var quinielaStatusText by mutableStateOf("Elige tu número de la suerte (00-9999)")
    var isQuinielaDrawing by mutableStateOf(false)
    var isLegalAcknowledgeChecked by mutableStateOf(false)

    // Results from Quiniela bolillero
    var quinielaDrawResults by mutableStateOf(emptyList<String>()) // 1st to 5th prize values

    fun playQuiniela() {
        if (!isLegalAcknowledgeChecked) {
            quinielaStatusText = "Debes validar con firma responsable los términos legales para jugar la quiniela."
            playSound("lose")
            return
        }

        val parsedNum = selectedQuinielaNumber.trim()
        if (parsedNum.isEmpty() || parsedNum.any { !it.isDigit() }) {
            quinielaStatusText = "Por favor ingresa un número válido (solo dígitos)"
            playSound("lose")
            return
        }

        val currentBalance = userAccount.value?.balance ?: 0.0
        if (currentBalance < quinielaBetAmount) {
            quinielaStatusText = "Créditos insuficientes"
            playSound("lose")
            return
        }

        viewModelScope.launch {
            isQuinielaDrawing = true
            quinielaStatusText = "Sorteando bolilleros de la lotería..."
            quinielaDrawResults = emptyList()

            // Bolillero draw simulation
            for (step in 1..8) {
                // Temp randomized draws
                val len = parsedNum.length
                val minRange = when (len) {
                    1 -> 0
                    2 -> 10
                    3 -> 100
                    else -> 1000
                }
                val maxRange = when (len) {
                    1 -> 9
                    2 -> 99
                    3 -> 999
                    else -> 9999
                }
                quinielaDrawResults = List(5) {
                    (minRange..maxRange).random().toString().padStart(len, '0')
                }
                playSound("spin")
                delay(150)
            }

            // Final real draw
            val len = parsedNum.length
            val padLen = if (len > 4) 4 else len
            val finalDraws = List(5) {
                when (padLen) {
                    1 -> (0..9).random().toString()
                    2 -> (0..99).random().toString().padStart(2, '0')
                    3 -> (0..999).random().toString().padStart(3, '0')
                    else -> (0..9999).random().toString().padStart(4, '0')
                }
            }
            quinielaDrawResults = finalDraws

            repository.processBet("QUINIELA", "Quiniela de la ${selectedQuinielaDrawing} #$parsedNum", quinielaBetAmount) {
                var won = false
                var multiplier = 0.0

                if (finalDraws[0] == parsedNum) {
                    won = true
                    // First place tier is high
                    multiplier = when (parsedNum.length) {
                        1 -> 7.0
                        2 -> 70.0
                        3 -> 500.0
                        else -> 3500.0
                    }
                } else if (finalDraws.contains(parsedNum)) {
                    // Secondary place tier
                    won = true
                    multiplier = when (parsedNum.length) {
                        1 -> 2.0
                        2 -> 10.0
                        3 -> 50.0
                        else -> 300.0
                    }
                }

                Pair(won, quinielaBetAmount * multiplier)
            }.also { (didWin, rewardAmt) ->
                isQuinielaDrawing = false
                if (didWin) {
                    playSound("jackpot")
                    quinielaStatusText = "¡FELICITACIONES! Tu número $parsedNum salió en pizarra. Ganaste $$rewardAmt"
                } else {
                    playSound("lose")
                    quinielaStatusText = "No tuviste coincidencias esta vez. Cabeza del sorteo: ${finalDraws[0]}"
                }
            }
        }
    }


    // --- SPORTS BETTING STATE ---
    var selectedMatchToBet by mutableStateOf<SportsMatch?>(null)
    var selectedOutcomeBet by mutableStateOf("HOME") // "HOME", "DRAW", "AWAY"
    var sportsBetAmount by mutableStateOf(20.0)
    var sportsStatusText by mutableStateOf("Elige un partido del tablero")

    fun placeSportsBet() {
        val match = selectedMatchToBet ?: return
        val currentBalance = userAccount.value?.balance ?: 0.0
        if (currentBalance < sportsBetAmount) {
            sportsStatusText = "Balance de créditos para apuesta insuficiente."
            playSound("lose")
            return
        }

        val label = when (selectedOutcomeBet) {
            "HOME" -> match.homeTeam
            "DRAW" -> "Empate"
            "AWAY" -> match.awayTeam
            else -> ""
        }

        val odds = when (selectedOutcomeBet) {
            "HOME" -> match.homeOdds
            "DRAW" -> match.drawOdds
            "AWAY" -> match.awayOdds
            else -> 1.0
        }

        viewModelScope.launch {
            repository.processBet("SPORTS", "Apuesta: ${match.homeTeam} vs ${match.awayTeam} - Seleccionó: $label", sportsBetAmount) {
                // Simulate resolving the bet
                delay(800)
                // Sports bet resolves after small delay
                val won = Math.random() > 0.45
                val payout = if (won) sportsBetAmount * odds else 0.0
                Pair(won, payout)
            }.also { (didWin, payoutAmt) ->
                if (didWin) {
                    playSound("win")
                    sportsStatusText = "¡Apuesta acertada! Has ganado $$payoutAmt con coeficiente de cuota $odds."
                } else {
                    playSound("lose")
                    sportsStatusText = "La apuesta no resultó. ¡El deporte tiene sorpresas!"
                }
            }
        }
    }


    // --- SCRATCH CARD GAME STATE ---
    var scratchedCells by mutableStateOf(BooleanArray(9) { false })
    var scratchCardValues by mutableStateOf(List(9) { "" })
    var currentScratchBet by mutableStateOf(10.0)
    var scratchStatusText by mutableStateOf("Paga la apuesta y rasca cada celda!")
    var isScratchCardActive by mutableStateOf(false)
    var wasScratchGameWon by mutableStateOf(false)
    var prizePoolRevealed by mutableStateOf(0.0)

    fun buyScratchCard() {
        val currentBalance = userAccount.value?.balance ?: 0.0
        if (currentBalance < currentScratchBet) {
            scratchStatusText = "Balance insuficiente para raspar"
            playSound("lose")
            return
        }

        // Generate prizes (either matching 3 values or random mismatch)
        val wins = Math.random() > 0.4
        val values = if (wins) {
            val prizes = listOf("$15", "$30", "$100", "$500")
            val prizeWinner = prizes.random()
            val list = mutableListOf<String>()
            repeat(3) { list.add(prizeWinner) }
            repeat(6) {
                var randomPrize = prizes.random()
                while (randomPrize == prizeWinner) {
                    randomPrize = prizes.random()
                }
                list.add(randomPrize)
            }
            list.shuffle()
            list
        } else {
            val list = listOf("$5", "$15", "$30", "$100", "$500").shuffled() +
                         listOf("$5", "$15", "$30", "$100").shuffled()
            list.take(9)
        }

        scratchCardValues = values
        scratchedCells = BooleanArray(9) { false }
        isScratchCardActive = true
        wasScratchGameWon = false
        prizePoolRevealed = 0.0
        scratchStatusText = "¡Tarjeta lista! Rasca frotando o tocando las 9 rejillas."
        playSound("spin")
    }

    fun scratchCell(index: Int) {
        if (!isScratchCardActive) return
        if (scratchedCells[index]) return

        val updated = scratchedCells.clone()
        updated[index] = true
        scratchedCells = updated
        playSound("click")

        // check if fully scratched
        if (scratchedCells.all { it }) {
            resolveScratchCard()
        }
    }

    private fun resolveScratchCard() {
        isScratchCardActive = false
        // Count payouts
        val counts = scratchCardValues.groupBy { it }.mapValues { it.value.size }
        val winningValueStr = counts.filter { it.value >= 3 }.keys.firstOrNull()

        viewModelScope.launch {
            if (winningValueStr != null) {
                val valueNum = winningValueStr.replace("$", "").toDouble()
                repository.processBet("CASINO", "Raspadita Bet: $$currentScratchBet", currentScratchBet) {
                    Pair(true, valueNum)
                }.also { (won, payoutAmt) ->
                    playSound("jackpot")
                    wasScratchGameWon = true
                    prizePoolRevealed = payoutAmt
                    scratchStatusText = "¡FELICITACIONES! Encontraste 3 coincidencias de $winningValueStr. Ganaste $$payoutAmt"
                }
            } else {
                repository.processBet("CASINO", "Raspadita Bet: $$currentScratchBet", currentScratchBet) {
                    Pair(false, 0.0)
                }.also {
                    playSound("lose")
                    wasScratchGameWon = false
                    scratchStatusText = "Lo siento, no hubo 3 números coincidentes. ¡Inténtalo de nuevo!"
                }
            }
        }
    }

    // --- SECURE PAYMENT GATEWAY STATE ---
    var paymentStatusText by mutableStateOf("Seleccione un método de depósito o retiro para comenzar (Cifrado AES-256 Activo)")
    var paymentInProgress by mutableStateOf(false)
    var isTransactionSecurelyEncrypted by mutableStateOf(false)

    fun executeSecureDeposit(amount: Double, method: String, reference: String, onComplete: () -> Unit = {}) {
        if (amount <= 0.0) {
            paymentStatusText = "El monto debe ser mayor a 0."
            playSound("lose")
            return
        }
        paymentInProgress = true
        paymentStatusText = "Iniciando pasarela de pago segura..."
        isTransactionSecurelyEncrypted = false
        
        viewModelScope.launch {
            delay(1000)
            paymentStatusText = "Estableciendo túnel de cifrado AES-256..."
            delay(1000)
            paymentStatusText = "Autorizando transacción con el banco/billetera..."
            delay(1000)
            
            val success = repository.secureDeposit(amount, method, reference)
            if (success) {
                isTransactionSecurelyEncrypted = true
                playSound("win")
                paymentStatusText = "¡Transacción autorizada y procesada de manera segura! Se depositaron $$amount créditos."
                onComplete()
            } else {
                playSound("lose")
                paymentStatusText = "Error al procesar el depósito. Verifique sus datos bancarios y reintente."
            }
            paymentInProgress = false
        }
    }

    fun executeSecureWithdraw(amount: Double, method: String, accountDetails: String, onComplete: () -> Unit = {}) {
        if (amount <= 0.0) {
            paymentStatusText = "El monto debe ser mayor a 0."
            playSound("lose")
            return
        }
        val currentBalance = userAccount.value?.balance ?: 0.0
        if (currentBalance < amount) {
            paymentStatusText = "Fondos insuficientes para realizar el retiro deseado ($$amount)."
            playSound("lose")
            return
        }
        
        paymentInProgress = true
        paymentStatusText = "Iniciando túnel de cifrado SSL/TLS de retiro..."
        isTransactionSecurelyEncrypted = false
        
        viewModelScope.launch {
            delay(1000)
            paymentStatusText = "Verificando firmas de criptografía híbrida con el banco receptor..."
            delay(1000)
            paymentStatusText = "Procesando transferencia segura del saldo..."
            delay(1000)
            
            val success = repository.secureWithdraw(amount, method, accountDetails)
            if (success) {
                isTransactionSecurelyEncrypted = true
                playSound("win")
                paymentStatusText = "¡Transacción de retiro encriptada procesada exitosamente! Retiro de $$amount enviado a $accountDetails."
                onComplete()
            } else {
                playSound("lose")
                paymentStatusText = "Error interno del sistema financiero. Verifique los límites de retiro diario."
            }
            paymentInProgress = false
        }
    }

    // Reset database to initial state
    fun resetSystem() {
        viewModelScope.launch {
            repository.resetAll()
            clearSignature()
            signerNameInput = ""
            isLegalAcknowledgeChecked = false
            playSound("click")
            feedbackMessage = "Sistema completo restablecido."
        }
    }
}
