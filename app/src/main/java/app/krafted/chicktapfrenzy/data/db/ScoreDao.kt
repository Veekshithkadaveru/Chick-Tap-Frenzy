package app.krafted.chicktapfrenzy.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreDao {
    @Insert
    suspend fun insertScore(entity: ScoreEntity)

    @Query("SELECT MAX(score) FROM scores")
    fun observeHighScore(): Flow<Int?>

    @Query("SELECT MAX(score) FROM scores")
    suspend fun getHighScoreOnce(): Int?
}
