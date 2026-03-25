-keep class com.jeanfit.app.data.** { *; }
-keep class com.jeanfit.app.domain.** { *; }
-keepclassmembers class ** {
    @com.squareup.moshi.Json <fields>;
}
-keep class com.squareup.moshi.** { *; }
