package com.example.mypawapp

import com.google.firebase.database.IgnoreExtraProperties // Kailangan i-import

@IgnoreExtraProperties // Idagdag ito
data class FeedingLog(
    val status: String? = null,
    val timestamp: Long? = null
) {
    constructor() : this(null, null)
}