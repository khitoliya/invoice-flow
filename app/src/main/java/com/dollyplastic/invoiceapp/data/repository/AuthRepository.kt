package com.dollyplastic.invoiceapp.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth
) {

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    suspend fun loginWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun loginWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).await()
    }

    suspend fun sendPasswordReset(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    suspend fun isUserAuthorized(): Boolean {
        val user = auth.currentUser ?: return false

        return try {
            val tokenResult = user.getIdToken(true).await()
            tokenResult.claims["allowed"] == true
        } catch (e: Exception) {
            false
        }
    }

    fun logout() {
        auth.signOut()
    }
}

