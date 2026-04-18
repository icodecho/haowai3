package evilcode.notification.hwpush.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import evilcode.notification.hwpush.model.PushMessage

@Dao
interface PushMessageDao {
    @Query("SELECT * FROM push_messages ORDER BY receivedTime DESC")
    suspend fun getAllMessages(): List<PushMessage>

    @Insert
    suspend fun insertMessage(message: PushMessage): Long

    @Delete
    suspend fun deleteMessages(messages: List<PushMessage>)

    @Query("DELETE FROM push_messages")
    suspend fun deleteAllMessages()

    @Query("SELECT * FROM push_messages WHERE id IN (:ids)")
    suspend fun getMessagesByIds(ids: List<Long>): List<PushMessage>
}
