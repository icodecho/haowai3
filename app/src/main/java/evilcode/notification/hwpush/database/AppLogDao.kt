package evilcode.notification.hwpush.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import evilcode.notification.hwpush.model.AppLog

@Dao
interface AppLogDao {
    @Query("SELECT * FROM app_logs ORDER BY timestamp DESC")
    suspend fun getAllLogs(): List<AppLog>

    @Insert
    suspend fun insertLog(log: AppLog): Long

    @Delete
    suspend fun deleteLogs(logs: List<AppLog>)

    @Query("DELETE FROM app_logs")
    suspend fun deleteAllLogs()

    @Query("SELECT * FROM app_logs WHERE id IN (:ids)")
    suspend fun getLogsByIds(ids: List<Long>): List<AppLog>
}
