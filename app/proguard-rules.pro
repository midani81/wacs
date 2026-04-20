# Add project specific ProGuard rules here.

# Keep Accessibility Service
-keep class com.midani.wacs.service.** { *; }

# Keep Room entities
-keep class com.midani.wacs.data.** { *; }

# Keep libphonenumber
-keep class com.google.i18n.phonenumbers.** { *; }
-keep class * implements com.google.i18n.phonenumbers.** { *; }

# Kotlin
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
