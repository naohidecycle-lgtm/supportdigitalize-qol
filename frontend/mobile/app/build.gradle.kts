plugins {
  id("com.android.application") version "8.6.1"
  id("org.jetbrains.kotlin.android") version "2.0.20"
  // Kotlin 2.0 + Compose では必須
  id("org.jetbrains.kotlin.plugin.compose") version "2.0.20"
  id("com.google.devtools.ksp") version "2.0.20-1.0.24"
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

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  /* -------------------------------------------------------------
   *  buildTypes（ここだけ変更 → コピペOK）
   *  debug / release どちらも“Prism”を指し続ける安全版です
   *  Retrofit の参照先を BuildConfig に切り替える前準備だけ整えます
   * ------------------------------------------------------------- */
  buildTypes {
    debug {
      // Prism モック用
      buildConfigField(
        "String",
        "API_BASE_URL",
        "\"http://10.0.2.2:4010/\""
      )
      // 画面に表示する接続先ラベル
      buildConfigField(
        "String",
        "BACKEND_ENV_LABEL",
        "\"Prism (mock)\""
      )
    }
    release {
      buildConfigField(
        "String",
        "API_BASE_URL",
        "\"https://rp2cotm1f6.execute-api.ap-northeast-1.amazonaws.com/\""
      )
      buildConfigField(
        "String",
        "BACKEND_ENV_LABEL",
        "\"API Gateway (default)\""    // ← ここをこう直す
      )

      signingConfig = signingConfigs.getByName("debug")
      isMinifyEnabled = false
    }
  }

  buildFeatures {
    compose = true
    buildConfig = true
  }

  composeOptions {
    kotlinCompilerExtensionVersion = "1.7.5"
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  kotlinOptions {
    jvmTarget = "17"
  }
}

dependencies {
  val compose = "1.7.5"

  implementation("androidx.core:core-ktx:1.13.1")
  implementation("androidx.activity:activity-compose:1.9.2")

  implementation("androidx.compose.ui:ui:$compose")
  implementation("androidx.compose.ui:ui-tooling-preview:$compose")
  implementation("androidx.compose.material3:material3:1.3.0")

  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
  implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
  implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
  implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

  // Retrofit + OkHttp + Gson
  implementation("com.squareup.retrofit2:retrofit:2.11.0")
  implementation("com.squareup.retrofit2:converter-gson:2.11.0")
  implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
  implementation("com.google.code.gson:gson:2.11.0")

  debugImplementation("androidx.compose.ui:ui-tooling:$compose")
  debugImplementation("androidx.compose.ui:ui-test-manifest:$compose")

  testImplementation("junit:junit:4.13.2")
  androidTestImplementation("androidx.test.ext:junit:1.2.1")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
