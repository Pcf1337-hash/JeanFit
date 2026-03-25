package com.jeanfit.app.data.repository

import com.jeanfit.app.data.db.dao.WeightDao
import com.jeanfit.app.data.db.entities.WeightEntry
import com.jeanfit.app.domain.usecase.CalcTrendUseCase
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeightRepository @Inject constructor(
    private val dao: WeightDao,
    private val calcTrend: CalcTrendUseCase
) {
    fun getAllEntries(): Flow<List<WeightEntry>> = dao.getAllEntries()
    fun getLatestEntry(): Flow<WeightEntry?> = dao.getLatestEntry()
    fun getTotalCount(): Flow<Int> = dao.getTotalEntryCount()

    suspend fun addEntry(weightKg: Float, date: LocalDate = LocalDate.now(), note: String? = null) {
        val previousTrend = dao.getLatestEntryOnce()?.trendWeightKg
        val trend = calcTrend.calculate(weightKg, previousTrend)
        val existing = dao.getEntryForDay(date.toEpochDay())
        if (existing != null) {
            dao.update(existing.copy(weightKg = weightKg, trendWeightKg = trend, note = note))
        } else {
            dao.insertOrReplace(
                WeightEntry(
                    weightKg = weightKg,
                    dateEpochDay = date.toEpochDay(),
                    note = note,
                    trendWeightKg = trend
                )
            )
        }
    }

    suspend fun deleteEntry(id: Long) = dao.delete(id)

    fun getEntriesInRange(startDay: Long, endDay: Long): Flow<List<WeightEntry>> =
        dao.getEntriesInRange(startDay, endDay)
}
