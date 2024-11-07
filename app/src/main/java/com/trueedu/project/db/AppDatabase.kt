package com.trueedu.project.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.trueedu.project.db.stocks.StockInfoLocalDao
import com.trueedu.project.model.dao.StockInfoLocal

@Database(entities = [StockInfoLocal::class], version = 1, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {
    abstract fun stockInfoLocalDao(): StockInfoLocalDao
}
