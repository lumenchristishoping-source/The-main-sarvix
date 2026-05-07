package com.sarvix.app.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date
import java.util.concurrent.TimeUnit

data class ClarifyLimit(
    val userId: String = "",
    val dailyCount: Int = 0,
    val maxDaily: Int = 5,
    @ServerTimestamp
    val resetTime: Date? = null
) {
    fun isLimitReached(): Boolean = dailyCount >= maxDaily
    
    fun shouldReset(): Boolean {
        resetTime?.let {
            val now = Date()
            val diffInMillis = now.time - it.time
            return diffInMillis >= TimeUnit.HOURS.toMillis(24)
        }
        return true
    }
    
    fun getRemainingTimeMillis(): Long {
        resetTime?.let {
            val now = Date()
            val resetMillis = it.time + TimeUnit.HOURS.toMillis(24)
            return maxOf(0, resetMillis - now.time)
        }
        return 0
    }
    
    fun getRemainingCount(): Int = maxOf(0, maxDaily - dailyCount)
}