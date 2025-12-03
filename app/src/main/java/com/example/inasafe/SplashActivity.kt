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

        // Ahora animamos el contenedor completo (Logo + Texto)
        val logoContainer = findViewById<View>(R.id.logoContainer)

        // Animación de "respiración" (escala) aplicada al contenedor
        val breathingAnimation = ObjectAnimator.ofPropertyValuesHolder(
            logoContainer,
            PropertyValuesHolder.ofFloat("scaleX", 1.1f), // Reduje un poco la escala para que no sea tan exagerada con el texto
            PropertyValuesHolder.ofFloat("scaleY", 1.1f)
        )
        breathingAnimation.duration = 1200 // Un poco más lento para que sea más suave
        breathingAnimation.repeatCount = ObjectAnimator.INFINITE
        breathingAnimation.repeatMode = ObjectAnimator.REVERSE
        breathingAnimation.interpolator = AccelerateDecelerateInterpolator()
        breathingAnimation.start()

        // Navegar a LoginActivity después de un tiempo
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 3000) // 3 segundos de espera
    }
}