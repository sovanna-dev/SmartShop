package com.smartshop.app.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Extension property - create DataStore attached to app context
val Context.dataStore: DataStore<Preferences>
    by preferencesDataStore("smartshop_prefs")
@Singleton
class DataStoreManager @Inject constructor(
    @ApplicationContext private val context: Context
){
    companion object {
        val FIRST_LAUNCH = booleanPreferencesKey("first_launch")
    }

    // Read first launch state as a Flow
    val isFirstLaunch: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[FIRST_LAUNCH] ?: true }

    //  Save that onboarding is done
    suspend fun setFirstLaunchDone() {
        context.dataStore.edit { prefs ->
            prefs[FIRST_LAUNCH] = false
        }
    }

}