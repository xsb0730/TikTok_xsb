package com.example.tiltok_xsb.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [CommentEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(UserBeanConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun commentDao(): CommentDao

    companion object {
        // 保证多线程可见性
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tiktok_database"
                )
                    .fallbackToDestructiveMigration()  // 开发阶段，版本升级时清空数据
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}