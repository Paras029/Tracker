package com.parasgarg.tracker.data.repository

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : AuthRepository {

    override fun isFirebaseAvailable(): Boolean =
        runCatching { FirebaseApp.getInstance(); true }.getOrDefault(false)

    private fun getAuth(): FirebaseAuth? =
        if (isFirebaseAvailable()) FirebaseAuth.getInstance() else null

    override fun observeCurrentUser(): Flow<AuthUser?> {
        val auth = getAuth() ?: return flowOf(null)
        return callbackFlow {
            val listener = FirebaseAuth.AuthStateListener { fa ->
                trySend(fa.currentUser?.toAuthUser())
            }
            auth.addAuthStateListener(listener)
            awaitClose { auth.removeAuthStateListener(listener) }
        }
    }

    override suspend fun signInWithGoogle(idToken: String): AuthUser? {
        val auth = getAuth() ?: return null
        return runCatching {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential).await().user?.toAuthUser()
        }.getOrNull()
    }

    override suspend fun signOut() {
        getAuth()?.signOut()
    }

    private fun com.google.firebase.auth.FirebaseUser.toAuthUser() =
        AuthUser(uid = uid, displayName = displayName, email = email)
}
