package evilcode.notification.hwpush.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import evilcode.notification.hwpush.model.PushMessage
import evilcode.notification.hwpush.model.AppLog

@Database(entities = [PushMessage::class, AppLog::class], version = 1, exportSchema = false)
abstract class HaoWaiDatabase : RoomDatabase() {
    abstract fun pushMessageDao(): PushMessageDao
    abstract fun appLogDao(): AppLogDao

    companion object {
        @Volatile
        private var INSTANCE: HaoWaiDatabase? = null

        fun getInstance(context: Context): HaoWaiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HaoWaiDatabase::class.java,
                    "haowai_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
