# ProGuard Rules for IR remote ac tv freeware

# 1. Kotlinx Serialization (Crucial for Navigation 3 and NavKey argument passing)
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class * implements kotlinx.serialization.KSerializer {
    *** INSTANCE;
}
-keep,allowobfuscation,allowshrinking @kotlinx.serialization.Serializable class *
-keepclassmembers @kotlinx.serialization.Serializable class * {
    *** Companion;
    *** $serializer;
}

# 2. Navigation 3 NavKeys
-keep class com.maahi.iractvremote.**NavKey { *; }
-keep class com.maahi.iractvremote.**NavKey$* { *; }

# 3. Application Data Models and Enums (Preserve enum names for JSON and SharedPreferences)
-keep enum com.maahi.iractvremote.model.** { *; }
-keep class com.maahi.iractvremote.data.SavedRemote { *; }
-keep class com.maahi.iractvremote.model.** { *; }

# 4. ZXing Barcode / QR Code Library
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**

# 5. Google Play In-App Billing Library
-keep class com.android.billingclient.api.** { *; }
-keep interface com.android.billingclient.api.** { *; }
-dontwarn com.android.billingclient.api.**

# 6. CameraX
-keep class androidx.camera.core.** { *; }
-keep class androidx.camera.camera2.** { *; }
-keep class androidx.camera.lifecycle.** { *; }
-keep class androidx.camera.view.** { *; }
-dontwarn androidx.camera.**

# 7. Hardware Consumer IR & Android System Services
-keep class android.hardware.ConsumerIrManager { *; }
-keep class android.hardware.ConsumerIrManager$CarrierFrequencyRange { *; }

# 8. Coroutines and Jetpack Compose
-dontwarn kotlinx.coroutines.**
-keepclassmembers class androidx.compose.ui.platform.AndroidComposeView { *; }
