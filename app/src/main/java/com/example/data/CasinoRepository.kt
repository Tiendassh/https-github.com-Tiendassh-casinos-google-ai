package com.example.data

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CasinoRepository(private val context: Context) {

    private val db: AppDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "casino_database"
        ).fallbackToDestructiveMigration().build()
    }

    private val userDao = db.userAccountDao()
    private val betDao = db.betHistoryDao()
    private val matchDao = db.sportsMatchDao()

    val userAccount: Flow<UserAccount?> = userDao.getUserAccount()
    val allBets: Flow<List<BetHistory>> = betDao.getAllBets()
    val allMatches: Flow<List<SportsMatch>> = matchDao.getAllMatches()

    suspend fun getOrCreateUserAccount(): UserAccount {
        val existing = userDao.getUserAccountDirect()
        if (existing != null) {
            return existing
        }
        val defaultUser = UserAccount()
        userDao.insertUserAccount(defaultUser)
        return defaultUser
    }

    suspend fun preseedMatchesIfEmpty() {
        val currentMatches = matchDao.getAllMatches().first()
        if (currentMatches.isEmpty()) {
            val list = listOf(
                SportsMatch(homeTeam = "Real Madrid", awayTeam = "Barcelona", homeOdds = 1.95, drawOdds = 3.40, awayOdds = 3.60, category = "Fútbol", matchTime = "En Vivo - 72'", isLive = true, scoreHome = 2, scoreAway = 1),
                SportsMatch(homeTeam = "Boca Juniors", awayTeam = "River Plate", homeOdds = 2.40, drawOdds = 2.90, awayOdds = 2.80, category = "Fútbol", matchTime = "En Vivo - 15'", isLive = true, scoreHome = 0, scoreAway = 0),
                SportsMatch(homeTeam = "Manchester City", awayTeam = "Liverpool", homeOdds = 2.15, drawOdds = 3.50, awayOdds = 3.10, category = "Fútbol", matchTime = "Hoy 16:00", isLive = false),
                SportsMatch(homeTeam = "LA Lakers", awayTeam = "Boston Celtics", homeOdds = 1.85, drawOdds = 13.0, awayOdds = 2.05, category = "Baloncesto", matchTime = "Hoy 21:30", isLive = false),
                SportsMatch(homeTeam = "Golden State", awayTeam = "Miami Heat", homeOdds = 1.70, drawOdds = 14.0, awayOdds = 2.20, category = "Baloncesto", matchTime = "Mañana 19:00", isLive = false),
                SportsMatch(homeTeam = "Alcaraz", awayTeam = "Sinner", homeOdds = 1.90, drawOdds = 0.0, awayOdds = 1.90, category = "Tenis", matchTime = "En Vivo - 3º Set", isLive = true, scoreHome = 1, scoreAway = 1),
                SportsMatch(homeTeam = "Djokovic", awayTeam = "Zverev", homeOdds = 1.50, drawOdds = 0.0, awayOdds = 2.60, category = "Tenis", matchTime = "Hoy 14:00", isLive = false),
                SportsMatch(homeTeam = "Max Verstappen", awayTeam = "Lewis Hamilton", homeOdds = 1.40, drawOdds = 0.0, awayOdds = 4.50, category = "F1", matchTime = "En Vivo - Vuelta 40/70", isLive = true)
            )
            matchDao.insertMatches(list)
        }
    }

    suspend fun signTerms(name: String, pointsX: String, pointsY: String) {
        val user = getOrCreateUserAccount()
        val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        val updated = user.copy(
            isTermsSigned = true,
            signedName = name,
            signedDate = formattedDate,
            signaturePointsX = pointsX,
            signaturePointsY = pointsY
        )
        userDao.updateUserAccount(updated)
    }

    suspend fun processBet(
        type: String,
        description: String,
        amount: Double,
        playAction: suspend () -> Pair<Boolean, Double> // returns Pair (DidWin, PayoutAmount)
    ): Pair<Boolean, Double> {
        val user = getOrCreateUserAccount()
        if (user.balance < amount) {
            return Pair(false, 0.0) // Insufficient funds but safety returned
        }

        // Deduct balance first
        val balanceAfterDeduct = user.balance - amount
        userDao.updateUserAccount(user.copy(balance = balanceAfterDeduct))

        // Play the game
        val (didWin, payout) = playAction()

        // Calculate losses and cashback
        val netResult = payout - amount
        val addedLoss = if (netResult < 0) -netResult else 0.0
        val addedWin = if (netResult > 0) netResult else 0.0

        val latestUser = getOrCreateUserAccount()
        val newLosses = latestUser.totalLosses + addedLoss
        val newWins = latestUser.totalWins + addedWin
        val finalBalance = latestUser.balance + payout

        // 15% cashback on accumulated losses
        val updatedCashback = (newLosses * 0.15) - (latestUser.totalWins + addedWin) * 0.05
        val validCashback = if (updatedCashback < 0) 0.0 else updatedCashback

        userDao.updateUserAccount(
            latestUser.copy(
                balance = finalBalance,
                totalLosses = newLosses,
                totalWins = newWins,
                claimableCashback = validCashback
            )
        )

        // Save bet history
        val status = if (payout > 0) "GANADA" else "PERDIDA"
        betDao.insertBet(
            BetHistory(
                type = type,
                description = description,
                amountBet = amount,
                payout = payout,
                status = status
            )
        )

        return Pair(didWin, payout)
    }

    // Recover Cashback System (Sistema de Recupero)
    suspend fun claimCashback(): Boolean {
        val user = getOrCreateUserAccount()
        val claimable = user.claimableCashback
        if (claimable <= 0) return false

        val newBalance = user.balance + claimable
        userDao.updateUserAccount(
            user.copy(
                balance = newBalance,
                claimableCashback = 0.0,
                // Soft-reset losses so they don't double claim on same losses
                totalLosses = 0.0,
                totalWins = 0.0
            )
        )

        betDao.insertBet(
            BetHistory(
                type = "RECOV",
                description = "Reclamo de recupero cashback (15% de pérdidas)",
                amountBet = 0.0,
                payout = claimable,
                status = "RECLAMADA"
            )
        )
        return true
    }

    // Emergency Protection Refill (Recupero de Emergencia)
    suspend fun getEmergencyCredits(): Boolean {
        val user = getOrCreateUserAccount()
        if (user.balance <= 5.0) {
            val newBalance = user.balance + 100.0
            userDao.updateUserAccount(user.copy(balance = newBalance))
            betDao.insertBet(
                BetHistory(
                    type = "RECOV",
                    description = "Recupero de emergencia (Bono de balance bajo)",
                    amountBet = 0.0,
                    payout = 100.0,
                    status = "BONIFICADO"
                )
            )
            return true
        }
        return false
    }

    // Manual Deposit Mock for testing betting system
    suspend fun depositCredits(amount: Double) {
        val user = getOrCreateUserAccount()
        userDao.updateUserAccount(user.copy(balance = user.balance + amount))
        betDao.insertBet(
            BetHistory(
                type = "DEPOSIT",
                description = "Cargar Crédito Virtual (Demo)",
                amountBet = 0.0,
                payout = amount,
                status = "COMPLETADO"
            )
        )
    }

    // Simulate match live updates & results resolver
    suspend fun simulateLiveMatchesTick() {
        val list = matchDao.getAllMatches().first()
        list.forEach { match ->
            if (match.isLive) {
                // Randomly update score
                val homeScore = match.scoreHome + if (Math.random() > 0.85) 1 else 0
                val awayScore = match.scoreAway + if (Math.random() > 0.85) 1 else 0
                matchDao.updateMatch(match.copy(scoreHome = homeScore, scoreAway = awayScore))
            }
        }
    }

    // Resolve a pending sports bet
    suspend fun resolveBet(betId: Int, outcome: String): String {
        // outcome: "HOME", "DRAW", "AWAY"
        val bet = betDao.getBetById(betId) ?: return "Bet not found"
        if (bet.status != "PENDING") return "Already resolved"

        // simple outcome simulator
        val won = Math.random() > 0.4
        val payout = if (won) bet.amountBet * 2.0 else 0.0

        val updatedBet = bet.copy(
            status = if (won) "WON" else "LOST",
            payout = payout
        )
        betDao.updateBet(updatedBet)

        if (won) {
            val user = getOrCreateUserAccount()
            userDao.updateUserAccount(user.copy(balance = user.balance + payout))
        }

        return if (won) "WON" else "LOST"
    }

    suspend fun resetAll() {
        betDao.clearHistory()
        val user = getOrCreateUserAccount()
        userDao.updateUserAccount(
            user.copy(
                balance = 1000.0,
                totalLosses = 0.0,
                totalWins = 0.0,
                claimableCashback = 0.0,
                isTermsSigned = false,
                signedName = null,
                signedDate = null,
                signaturePointsX = null,
                signaturePointsY = null
            )
        )
    }
}
