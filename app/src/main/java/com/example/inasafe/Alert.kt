package com.example.inasafe

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Alert(
    val title: String? = null,
    val description: String? = null,
    val time: String? = null,
    val status: String? = null,
    val type: String? = null,
    val x: Float? = null,
    val y: Float? = null,
    val userId: String? = null
)