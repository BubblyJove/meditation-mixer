# Core Data ProGuard Rules
# Keep Room entities
-keep class com.meditationmixer.core.data.database.entity.** { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
