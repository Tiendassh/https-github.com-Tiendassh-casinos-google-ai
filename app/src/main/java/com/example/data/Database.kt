package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "user_account")
data class UserAccount(
    @PrimaryKey val id: Int = 1,
    val balance: Double = 1000.0, // Start with 1,000 free bonus credits
    val totalLosses: Double = 0.0,
    val totalWins: Double = 0.0,
    val claimableCashback: Double = 0.0,
    val isTermsSigned: Boolean = false,
    val signedName: String? = null,
    val signedDate: String? = null,
    val signaturePointsX: String? = null, // Comma separated list of X coords
    val signaturePointsY: String? = null  // Comma separated list of Y coords
)

@Entity(tableName = "bet_history")
data class BetHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "CASINO", "SPORTS", "QUINIELA"
    val description: String, // "Blackjack: $150 bet", "Real Madrid Win", "Quiniela: 42, 18, 93"
    val amountBet: Double,
    val payout: Double, // 0 if lost, positive if won
    val status: String, // "PENDING", "WON", "LOST"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "sports_match")
data class SportsMatch(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val homeTeam: String,
    val awayTeam: String,
    val homeOdds: Double,
    val drawOdds: Double,
    val awayOdds: Double,
    val category: String, // "Fútbol", "Baloncesto", "Tenis", "F1"
    val matchTime: String, // "En Vivo - 67'", "Hoy 20:00", etc.
    val isLive: Boolean = false,
    val scoreHome: Int = 0,
    val scoreAway: Int = 0
)

@Dao
interface UserAccountDao {
    @Query("SELECT * FROM user_account WHERE id = 1 LIMIT 1")
    fun getUserAccount(): Flow<UserAccount?>

    @Query("SELECT * FROM user_account WHERE id = 1 LIMIT 1")
    suspend fun getUserAccountDirect(): UserAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAccount(userAccount: UserAccount)

    @Update
    suspend fun updateUserAccount(userAccount: UserAccount)
}

@Dao
interface BetHistoryDao {
    @Query("SELECT * FROM bet_history ORDER BY timestamp DESC")
    fun getAllBets(): Flow<List<BetHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBet(bet: BetHistory): Long

    @Update
    suspend fun updateBet(bet: BetHistory)

    @Query("SELECT * FROM bet_history WHERE id = :id")
    suspend fun getBetById(id: Int): BetHistory?

    @Query("DELETE FROM bet_history")
    suspend fun clearHistory()
}

@Dao
interface SportsMatchDao {
    @Query("SELECT * FROM sports_match")
    fun getAllMatches(): Flow<List<SportsMatch>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: SportsMatch)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatches(matches: List<SportsMatch>)

    @Update
    suspend fun updateMatch(match: SportsMatch)

    @Query("DELETE FROM sports_match")
    suspend fun clearMatches()
}

@Database(entities = [UserAccount::class, BetHistory::class, SportsMatch::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userAccountDao(): UserAccountDao
    abstract fun betHistoryDao(): BetHistoryDao
    abstract fun sportsMatchDao(): SportsMatchDao
}
