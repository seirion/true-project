package com.trueedu.project.repository.local

import com.trueedu.project.db.stocks.StockInfoLocalDao
import com.trueedu.project.model.dao.StockInfoLocal
import javax.inject.Inject

class StockLocal @Inject constructor(
    private val stockInfoLocalDao: StockInfoLocalDao
) {
    suspend fun getAllStocks(): List<StockInfoLocal> {
        return stockInfoLocalDao.getAllStocks()
    }

    suspend fun setAllStocks(stocks: List<StockInfoLocal>)  {
        return stockInfoLocalDao.insertAll(stocks)
    }
}
