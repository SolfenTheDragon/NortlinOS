// Generates the app's Baseline Profile on a connected watch or Wear emulator (API 33+):
//   ./gradlew :app:generateBaselineProfile
// The result lands in app/src/release/generated/baselineProfiles/ and is committed.
plugins {
    id("com.android.test")
    id("androidx.baselineprofile")
}

android {
    namespace = "com.nortlinos.wearos.baselineprofile"
    compileSdk = 37

    defaultConfig {
        minSdk = 33
        targetSdk = 35
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    targetProjectPath = ":app"
}

baselineProfile {
    // Use whichever device is plugged in rather than a Gradle-managed emulator: the journey
    // needs a signed-in server, which only a real setup has.
    useConnectedDevices = true
}

dependencies {
    implementation("androidx.test.ext:junit:1.3.0")
    implementation("androidx.test.uiautomator:uiautomator:2.3.0")
    implementation("androidx.benchmark:benchmark-macro-junit4:1.5.0")
}
