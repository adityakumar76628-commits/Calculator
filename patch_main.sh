#!/bin/bash
sed -i 's/import android.os.Bundle/import android.os.Bundle\nimport com.google.android.gms.ads.MobileAds\nimport com.google.android.gms.ads.AdRequest\nimport com.google.android.gms.ads.AdSize\nimport com.google.android.gms.ads.AdView\nimport androidx.compose.ui.viewinterop.AndroidView/' app/src/main/java/com/example/MainActivity.kt

sed -i 's/super.onCreate(savedInstanceState)/super.onCreate(savedInstanceState)\n        MobileAds.initialize(this) {}/' app/src/main/java/com/example/MainActivity.kt
