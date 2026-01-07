import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.ahmedsamy.purelink"
    compileSdk = 36

    dependenciesInfo {
        // Privacy: Disable dependency metadata for IzzyOnDroid compliance
        includeInApk = false
        includeInBundle = false
    }

    defaultConfig {
                applicationId = "com.ahmedsamy.purelink"
        
        minSdk = 26
        targetSdk = 36
        
        versionCode = 3
        versionName = "2.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        // 1. Create dummy config to prevent CI/CD build failures
        create("release") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }

        // 2. Load local signing keys if available
        val props = Properties()
        val localProperties = File(rootDir, "signing.properties")
        if (localProperties.exists()) {
            localProperties.inputStream().use { props.load(it) }
            getByName("release") {
                storeFile = file(props.getProperty("release.store.file"))
                storePassword = props.getProperty("release.store.password")
                keyAlias = props.getProperty("release.key.alias")
                keyPassword = props.getProperty("release.key.password")
            }
        } else {
            println("NOTE: signing.properties not found. Using default keys for build.")
        }
    }

    buildTypes {
        getByName("debug") {
            // Keep debug builds fast and open
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }

        getByName("release") {
            // Optimize APK size (~2MB) using R8
            isMinifyEnabled = true
            isShrinkResources = true
            
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            isDebuggable = false
            signingConfig = signingConfigs.getByName("release")
        }
    }

    // Auto-rename output APK: PureLink-v2.0.0.apk
    applicationVariants.configureEach {
        outputs.mapNotNull { it as? BaseVariantOutputImpl }.forEach {
            it.outputFileName = "PureLink-v${versionName}.apk"
        }
    }

    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_21)
        targetCompatibility(JavaVersion.VERSION_21)
    }

    kotlin { jvmToolchain(21) }
    buildFeatures { compose = true }
}

dependencies {
    implementation("androidx.core:core-ktx:1.17.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")

    // Jetpack Compose
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.12.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.work:work-runtime-ktx:2.10.0")
    
    debugImplementation("androidx.compose.ui:ui-tooling")
}