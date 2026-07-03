package com.parasgarg.tracker.data.repository

import com.parasgarg.tracker.data.local.dao.NutritionDao
import com.parasgarg.tracker.data.local.entity.FoodLogEntity
import com.parasgarg.tracker.data.model.SyncStatus
import com.parasgarg.tracker.data.model.domain.MealType
import com.parasgarg.tracker.data.model.domain.NutritionEntry
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NutritionRepositoryImpl @Inject constructor(
    private val dao: NutritionDao,
) : NutritionRepository {

    override fun observeEntriesForDate(date: LocalDate): Flow<List<NutritionEntry>> =
        dao.observeForDate(date.toEpochDay()).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun observeEntriesForDateRange(from: LocalDate, to: LocalDate): Flow<List<NutritionEntry>> =
        dao.observeForDateRange(from.toEpochDay(), to.toEpochDay()).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun logFood(entry: NutritionEntry) {
        dao.upsertFoodLog(entry.toEntity())
    }

    override suspend fun deleteEntry(id: String) {
        dao.softDeleteFoodLog(id, Instant.now().toEpochMilli())
    }

    private fun FoodLogEntity.toDomain() = NutritionEntry(
        id = id,
        date = date,
        mealType = runCatching { MealType.valueOf(mealType) }.getOrDefault(MealType.SNACK),
        foodName = foodName,
        servingGrams = servingGrams,
        caloriesKcal = caloriesKcal,
        proteinG = proteinG,
        carbsG = carbsG,
        fatG = fatG,
        loggedAt = loggedAt,
    )

    private fun NutritionEntry.toEntity(): FoodLogEntity {
        val now = Instant.now()
        return FoodLogEntity(
            id = id,
            date = date,
            mealType = mealType.name,
            foodName = foodName,
            servingGrams = servingGrams,
            caloriesKcal = caloriesKcal,
            proteinG = proteinG,
            carbsG = carbsG,
            fatG = fatG,
            loggedAt = loggedAt,
            syncStatus = SyncStatus.PENDING,
            isDeleted = false,
            updatedAt = now,
        )
    }
}
