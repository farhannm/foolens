package com.proyek.foolens

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class FoolensApplication : Application() {
    private var currentActivity: Activity? = null

    fun getCurrentActivity(): Activity? = currentActivity

    @Inject
    lateinit var appInitializer: AppInitializer

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase first
        initializeFirebase()

        // Register activity lifecycle callbacks
        registerActivityLifecycleCallbacks(activityLifecycleCallbacks)

        // Initialize app components
        initializeApp()
    }

    private fun initializeFirebase() {
        try {
            FirebaseApp.initializeApp(this)
            val playServicesAvailable = GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS
            Log.d(TAG, "Google Play Services available: $playServicesAvailable")
            if (!playServicesAvailable) {
                Log.e(TAG, "Google Play Services not available")
            }
            if (BuildConfig.DEBUG) {
                FirebaseAuth.getInstance().setLanguageCode("id")
            }
            Log.d(TAG, "Firebase initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Firebase initialization failed", e)
        }
    }

    private val activityLifecycleCallbacks = object : ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            Log.d(TAG, "Activity created: ${activity.javaClass.simpleName}")
        }

        override fun onActivityStarted(activity: Activity) {
            Log.d(TAG, "Activity started: ${activity.javaClass.simpleName}")
        }

        override fun onActivityResumed(activity: Activity) {
            currentActivity = activity
            Log.d(TAG, "Activity resumed: ${activity.javaClass.simpleName}")

            // Start SmsRetriever when activity is resumed
            startSmsRetrieverForActivity(activity)
        }

        override fun onActivityPaused(activity: Activity) {
            if (currentActivity === activity) {
                currentActivity = null
            }
            Log.d(TAG, "Activity paused: ${activity.javaClass.simpleName}")
        }

        override fun onActivityStopped(activity: Activity) {
            Log.d(TAG, "Activity stopped: ${activity.javaClass.simpleName}")
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            // No implementation needed
        }

        override fun onActivityDestroyed(activity: Activity) {
            Log.d(TAG, "Activity destroyed: ${activity.javaClass.simpleName}")
        }
    }

    private fun startSmsRetrieverForActivity(activity: Activity) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = SmsRetriever.getClient(activity)
                val task = client.startSmsRetriever()

                task.addOnSuccessListener {
                    Log.d(TAG, "SmsRetriever started successfully for ${activity.javaClass.simpleName}")
                    Timber.d("SmsRetriever started successfully for ${activity.javaClass.simpleName}")
                }.addOnFailureListener { exception ->
                    Log.e(TAG, "Failed to start SmsRetriever for ${activity.javaClass.simpleName}", exception)
                    Timber.e(exception, "Failed to start SmsRetriever for ${activity.javaClass.simpleName}")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Exception starting SmsRetriever", e)
                Timber.e(e, "Exception starting SmsRetriever")
            }
        }
    }

    private fun initializeApp() {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                // Initialize Timber for logging
                if (BuildConfig.DEBUG) {
                    Timber.plant(Timber.DebugTree())
                    Log.d(TAG, "Timber initialized for debug build")
                }

                // Initialize app components
                appInitializer.initialize()

                Log.d(TAG, "App initialization completed successfully")

            } catch (e: Exception) {
                Log.e(TAG, "App initialization failed", e)
                Timber.e(e, "App initialization failed")
            }
        }
    }

    companion object {
        private const val TAG = "FoolensApplication"
    }
}

// Interface for app initialization
interface AppInitializer {
    suspend fun initialize()
}

// Default implementation of AppInitializer
class DefaultAppInitializer @Inject constructor(
    // Add any dependencies needed for initialization
) : AppInitializer {

    override suspend fun initialize() {
        Log.d(TAG, "DefaultAppInitializer: Starting app initialization")

        try {
            // Perform app-wide initialization tasks
            initializeLogging()
            initializeAppSettings()
            // Add more initialization tasks as needed

            Log.d(TAG, "DefaultAppInitializer: App initialization completed")
        } catch (e: Exception) {
            Log.e(TAG, "DefaultAppInitializer: Initialization failed", e)
            throw e
        }
    }

    private suspend fun initializeLogging() {
        // Set up logging configuration
        Log.d(TAG, "Logging system initialized")
    }

    private suspend fun initializeAppSettings() {
        // Initialize app-wide settings
        Log.d(TAG, "App settings initialized")
    }

    companion object {
        private const val TAG = "DefaultAppInitializer"
    }
}