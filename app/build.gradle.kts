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
    
    defaultConfig {
        applicationId = "com.ahmedsamy.purelink"
        minSdk = 26
        targetSdk = 36
        versionCode = 5
        versionName = "4.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Keep only English and Arabic resources to drastically reduce APK/AAB size
        resourceConfigurations += listOf("en", "ar")
    }

    signingConfigs {
        // Load local signing keys if available
        val props = Properties()
        val localProperties = File(rootDir, "signing.properties")
        if (localProperties.exists()) {
            localProperties.inputStream().use { props.load(it) }
            create("release") {
                storeFile = file(props.getProperty("release.store.file"))
                storePassword = props.getProperty("release.store.password")
                keyAlias = props.getProperty("release.key.alias")
                keyPassword = props.getProperty("release.key.password")
            }
        } else {
            println("NOTE: signing.properties not found. Building unsigned APK.")
        }
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }

        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            isDebuggable = false
            
            // Apply signing config ONLY if local properties exist
            val localProperties = File(rootDir, "signing.properties")
            if (localProperties.exists()) {
                signingConfig = signingConfigs.getByName("release")
            } else {
                signingConfig = null
            }

            // --- Dependency Info Configuration ---
            // Uncomment the block below when building for Google Play to maximize privacy and reduce size.
            // Comment it out when building for IzzyOnDroid/F-Droid so their bots can analyze dependencies.
            /*
            dependenciesInfo {
                includeInApk = false
                includeInBundle = false
            }
            */
        }
    }

    // Auto-rename output APKs dynamically based on their build variant
    applicationVariants.configureEach {
        val variantName = this.name
        outputs.mapNotNull { it as? BaseVariantOutputImpl }.forEach {
            val suffix = if (variantName == "release") "-Official" else "-Debug"
            it.outputFileName = "PureLink-v${versionName}${suffix}.apk"
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
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.work:work-runtime-ktx:2.10.0")
    
    debugImplementation("androidx.compose.ui:ui-tooling")
}