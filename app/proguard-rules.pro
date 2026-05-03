# Mantener las interfaces AIDL del SDK Urovo
-keep class com.ubx.usdk.** { *; }
-keep interface com.ubx.usdk.** { *; }

# Mantener los modelos de datos
-keep class com.ubx.rfid.p006ui.scan.ScanModel { *; }

# Mantener enums
-keepclassmembers enum * { *; }
