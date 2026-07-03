package com.parasgarg.tracker.data.repository

import com.parasgarg.tracker.data.local.dao.UserProfileDao
import com.parasgarg.tracker.data.local.entity.UserProfileEntity
import com.parasgarg.tracker.data.model.domain.UserProfile
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserProfileRepositoryImpl @Inject constructor(
    private val dao: UserProfileDao,
) : UserProfileRepository {

    override fun observe(): Flow<UserProfile?> = dao.observe().map { it?.toDomain() }

    override suspend fun save(profile: UserProfile) {
        dao.upsert(profile.toEntity())
    }
}

private fun UserProfileEntity.toDomain() = UserProfile(
    name = name,
    heightCm = heightCm,
    weightKg = weightKg,
    activityLevel = activityLevel,
    fitnessGoal = fitnessGoal,
    targetCaloriesKcal = targetCaloriesKcal,
    targetProteinG = targetProteinG,
    targetCarbsG = targetCarbsG,
    targetFatG = targetFatG,
    geminiApiKey = geminiApiKey,
    isGeminiOfflineMode = isGeminiOfflineMode,
)

private fun UserProfile.toEntity() = UserProfileEntity(
    id = 1,
    name = name,
    heightCm = heightCm,
    weightKg = weightKg,
    activityLevel = activityLevel,
    fitnessGoal = fitnessGoal,
    targetCaloriesKcal = targetCaloriesKcal,
    targetProteinG = targetProteinG,
    targetCarbsG = targetCarbsG,
    targetFatG = targetFatG,
    geminiApiKey = geminiApiKey,
    isGeminiOfflineMode = isGeminiOfflineMode,
    updatedAt = Instant.now(),
)
