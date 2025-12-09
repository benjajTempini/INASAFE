package com.example.inasafe

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val logoContainer = findViewById<View>(R.id.logoContainer)

        // Make animation more pronounced: Scale from 1.0 to 1.2
        val breathingAnimation = ObjectAnimator.ofPropertyValuesHolder(
            logoContainer,
            PropertyValuesHolder.ofFloat("scaleX", 1.0f, 1.2f),
            PropertyValuesHolder.ofFloat("scaleY", 1.0f, 1.2f)
        )
        breathingAnimation.duration = 1000 // Faster pulse (1 second)
        breathingAnimation.repeatCount = ObjectAnimator.INFINITE
        breathingAnimation.repeatMode = ObjectAnimator.REVERSE
        breathingAnimation.interpolator = AccelerateDecelerateInterpolator()
        breathingAnimation.start()

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 3000)
    }
}