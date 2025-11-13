plugins {
  id("com.android.application") version "8.6.1"
  id("org.jetbrains.kotlin.android") version "2.0.20"
  // Kotlin 2.0 + Compose では必須
  id("org.jetbrains.kotlin.plugin.compose") version "2.0.20"
}

android {
  namespace = "com.sd.mobile"
  compileSdk = 35

  defaultConfig {
    applicationId = "com.sd.mobile"
    minSdk = 26
    targetSdk = 35
    versionCode = 1
    versionName = "0.1.0"
  }

  buildTypes {
    debug { isMinifyEnabled = false }
    release {
      isMinifyEnabled = true
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
    }
  }

  buildFeatures { compose = true }

  // Kotlin 2.0 以降は Compose compiler の個別指定は不要（プラグインが面倒を見ます）

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  kotlinOptions { jvmTarget = "17" }
}

dependencies {
  val compose = "1.7.5"

  // Compose & Material3
  implementation("androidx.activity:activity-compose:1.9.2")
  implementation("androidx.compose.ui:ui:$compose")
  implementation("androidx.compose.ui:ui-tooling-preview:$compose")
  implementation("androidx.compose.material3:material3:1.3.0")

  // Lifecycle / ViewModel（2.8.7に統一）
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
  implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
  implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
  implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    // Retrofit + Gson + OkHttp
  implementation("com.squareup.retrofit2:retrofit:2.11.0")
  implementation("com.squareup.retrofit2:converter-gson:2.11.0")
  implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
  implementation("com.google.code.gson:gson:2.11.0")


  // 開発時ツール
  debugImplementation("androidx.compose.ui:ui-tooling:$compose")
  debugImplementation("androidx.compose.ui:ui-test-manifest:$compose")
}
