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
        // Disables dependency metadata for privacy (IzzyOnDroid Req)
        includeInApk = false
        includeInBundle = false
    }

    defaultConfig {
        applicationId = "com.ahmedsamy.purelink"
        minSdk = 26
        targetSdk = 36
        versionCode = 2 // حدثته لـ 2 عشان التحديث الجديد
        versionName = "1.0.1" // حدثته لـ 1.0.1 زي ما اتفقنا
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // كان هنا فيه قوس زيادة } أنا شلته، وده اللي كان خارب الدنيا

    signingConfigs {
        val props = Properties()
        val localProperties = File(rootDir, "signing.properties")
        if (localProperties.exists()) {
            localProperties.inputStream().use { props.load(it) }
        }
        getByName("debug") {
            storeFile = file(props.getProperty("debug.store.file"))
            storePassword = props.getProperty("debug.store.password")
            keyAlias = props.getProperty("debug.key.alias")
            keyPassword = props.getProperty("debug.key.password")
        }
        create("release") {
            storeFile = file(props.getProperty("release.store.file"))
            storePassword = props.getProperty("release.store.password")
            keyAlias = props.getProperty("release.key.alias")
            keyPassword = props.getProperty("release.key.password")
        }
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            signingConfig = signingConfigs.getByName("debug")
        }

        getByName("release") {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            isDebuggable = false
            signingConfig = signingConfigs.getByName("release")
        }
    }

    // كود التسمية التلقائية (عشان يطلع باسم التطبيق)
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
//    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.activity:activity-compose:1.12.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
}