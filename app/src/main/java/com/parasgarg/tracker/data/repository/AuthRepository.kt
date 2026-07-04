package com.parasgarg.tracker.data.repository

import kotlinx.coroutines.flow.Flow

data class AuthUser(val uid: String, val displayName: String?, val email: String?)

interface AuthRepository {
    fun observeCurrentUser(): Flow<AuthUser?>
    suspend fun signInWithGoogle(idToken: String): AuthUser?
    suspend fun signOut()
    fun isFirebaseAvailable(): Boolean
}
