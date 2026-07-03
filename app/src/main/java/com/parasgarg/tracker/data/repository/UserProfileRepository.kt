package com.parasgarg.tracker.data.repository

import com.parasgarg.tracker.data.model.domain.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {
    fun observe(): Flow<UserProfile?>
    suspend fun save(profile: UserProfile)
}
