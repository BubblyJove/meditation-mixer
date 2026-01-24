package com.meditationmixer.core.common

import java.util.Calendar
import java.util.concurrent.TimeUnit

object TimeUtils {
    
    fun formatDuration(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
    
    fun formatDurationLong(milliseconds: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60
        
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
    
    fun isWithinBedtimeWindow(
        timestamp: Long,
        startHour: Int,
        endHour: Int
    ): Boolean {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
        }
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        
        return if (startHour > endHour) {
            // Window spans midnight (e.g., 20:00 to 06:00)
            hour >= startHour || hour < endHour
        } else {
            // Window within same day
            hour in startHour until endHour
        }
    }
    
    fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = timestamp1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = timestamp2 }
        
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
    
    fun isConsecutiveDay(previousTimestamp: Long, currentTimestamp: Long): Boolean {
        val prevCal = Calendar.getInstance().apply { timeInMillis = previousTimestamp }
        val currCal = Calendar.getInstance().apply { timeInMillis = currentTimestamp }
        
        prevCal.add(Calendar.DAY_OF_YEAR, 1)
        
        return prevCal.get(Calendar.YEAR) == currCal.get(Calendar.YEAR) &&
                prevCal.get(Calendar.DAY_OF_YEAR) == currCal.get(Calendar.DAY_OF_YEAR)
    }
    
    fun minutesToMillis(minutes: Int): Long = minutes * 60 * 1000L
    
    fun secondsToMillis(seconds: Int): Long = seconds * 1000L
}
