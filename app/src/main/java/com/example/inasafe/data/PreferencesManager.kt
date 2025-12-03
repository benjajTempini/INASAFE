package com.example.inasafe.data

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getFavoriteServices(): Set<String> {
        return sharedPreferences.getStringSet(KEY_FAVORITE_SERVICES, emptySet()) ?: emptySet()
    }

    fun addFavoriteService(serviceName: String) {
        val favorites = getFavoriteServices().toMutableSet()
        favorites.add(serviceName)
        sharedPreferences.edit().putStringSet(KEY_FAVORITE_SERVICES, favorites).apply()
    }

    fun removeFavoriteService(serviceName: String) {
        val favorites = getFavoriteServices().toMutableSet()
        favorites.remove(serviceName)
        sharedPreferences.edit().putStringSet(KEY_FAVORITE_SERVICES, favorites).apply()
    }

    companion object {
        private const val PREFS_NAME = "inasafe_prefs"
        private const val KEY_FAVORITE_SERVICES = "favorite_services"
    }
}