package evilcode.notification.hwpush.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "push_messages")
data class PushMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val messageId: String?,
    val title: String?,
    val body: String?,
    val data: String?,
    val token: String?,
    val collapseKey: String?,
    val sentTime: Long,
    val receivedTime: Long = System.currentTimeMillis()
)
