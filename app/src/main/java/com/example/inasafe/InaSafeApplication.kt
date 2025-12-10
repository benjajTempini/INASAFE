package com.example.inasafe

import android.app.Application
import androidx.preference.PreferenceManager
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.ktor.client.engine.okhttp.OkHttp
import org.osmdroid.config.Configuration

class InaSafeApplication : Application() {

    companion object {
        lateinit var supabaseClient: SupabaseClient
            private set
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase first, as other libraries may depend on it.
        FirebaseApp.initializeApp(this)
        val firebaseAppCheck = FirebaseAppCheck.getInstance()

        firebaseAppCheck.installAppCheckProviderFactory(
            if (BuildConfig.DEBUG) {
                DebugAppCheckProviderFactory.getInstance()
            } else {
                PlayIntegrityAppCheckProviderFactory.getInstance()
            }
        )

        // Initialize OSMDroid configuration
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))

        // Initialize Supabase with credentials from BuildConfig
        supabaseClient = createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_KEY
        ) {
            install(Postgrest)
            install(Realtime)
            httpEngine = OkHttp.create() // Explicitly set the HTTP engine
        }
    }
}