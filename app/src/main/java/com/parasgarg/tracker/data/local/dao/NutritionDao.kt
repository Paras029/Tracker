package com.parasgarg.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.parasgarg.tracker.data.local.entity.CustomMealEntity
import com.parasgarg.tracker.data.local.entity.FoodLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NutritionDao {

    @Query("SELECT * FROM food_log WHERE date = :epochDay AND isDeleted = 0 ORDER BY loggedAt ASC")
    fun observeForDate(epochDay: Long): Flow<List<FoodLogEntity>>

    @Upsert
    suspend fun upsertFoodLog(entity: FoodLogEntity)

    @Query("UPDATE food_log SET isDeleted = 1, syncStatus = 'PENDING', updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteFoodLog(id: String, updatedAt: Long)

    @Query("SELECT * FROM custom_meals WHERE isDeleted = 0 ORDER BY name ASC")
    fun observeCustomMeals(): Flow<List<CustomMealEntity>>

    @Upsert
    suspend fun upsertCustomMeal(entity: CustomMealEntity)

    @Query("UPDATE custom_meals SET isDeleted = 1, syncStatus = 'PENDING', updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteCustomMeal(id: String, updatedAt: Long)
}
