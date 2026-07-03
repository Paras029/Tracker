package com.parasgarg.tracker.data.repository

import com.parasgarg.tracker.data.model.domain.NutritionEntry
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface NutritionRepository {
    fun observeEntriesForDate(date: LocalDate): Flow<List<NutritionEntry>>
    fun observeEntriesForDateRange(from: LocalDate, to: LocalDate): Flow<List<NutritionEntry>>
    suspend fun logFood(entry: NutritionEntry)
    suspend fun deleteEntry(id: String)
}
