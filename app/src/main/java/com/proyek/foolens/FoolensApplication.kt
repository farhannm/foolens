package com.proyek.foolens
import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class FoolensApplication : Application() {

    @Inject
    lateinit var appInitializer: AppInitializer

    override fun onCreate() {
        super.onCreate()

        // Initialize app-wide components
        initializeApp()
    }

    private fun initializeApp() {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                // Perform any necessary app-wide initialization
                appInitializer.initialize()
            } catch (e: Exception) {
                Log.e("MyApplication", "Initialization failed", e)
            }
        }
    }
}

// Interface for app initialization
interface AppInitializer {
    suspend fun initialize()
}

// Example implementation (you can create a concrete implementation in a separate file)
class DefaultAppInitializer @Inject constructor(
    // Add any dependencies needed for initialization
) : AppInitializer {
    override suspend fun initialize() {
        // Perform app-wide initialization tasks
        // For example:
        // - Initialize logging
        // - Set up crash reporting
        // - Perform initial data setup
        // - Configure app-wide settings
        Log.d("AppInitializer", "App initialization started")

        // Add your initialization logic here
    }
}