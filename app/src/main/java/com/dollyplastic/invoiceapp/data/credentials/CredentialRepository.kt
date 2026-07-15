package com.dollyplastic.invoiceapp.data.credentials

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

@Singleton
class CredentialRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_credentials", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val key = "saved_credentials"

    fun getAll(): List<Credential> {
        val json = prefs.getString(key, null) ?: return emptyList()
        val type = object : TypeToken<List<Credential>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun add(credential: Credential) {
        val current = getAll().toMutableList()
        current.add(credential)
        saveList(current)
    }

    fun delete(id: String) {
        val current = getAll().toMutableList()
        current.removeAll { it.id == id }
        saveList(current)
    }

    fun update(credential: Credential) {
        val current = getAll().toMutableList()
        val index = current.indexOfFirst { it.id == credential.id }
        if (index != -1) {
            current[index] = credential
            saveList(current)
        }
    }

    private fun saveList(list: List<Credential>) {
        val json = gson.toJson(list)
        prefs.edit { putString(key, json) }
    }
}
