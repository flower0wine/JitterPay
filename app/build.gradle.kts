plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.android)
}

/**
 * 版本号获取工具函数
 * 优先级: CI环境变量 > 命令行参数 > gradle.properties > 报错
 */
fun getVersionName(): String {
    // 1. CI 环境变量: ORG_GRADLE_PROJECT_VERSION_NAME
    val envValue = System.getenv("ORG_GRADLE_PROJECT_VERSION_NAME")
    if (!envValue.isNullOrEmpty()) return envValue

    // 2. 命令行参数: -PVERSION_NAME
    if (project.hasProperty("VERSION_NAME")) {
        return project.property("VERSION_NAME") as String
    }

    // 3. gradle.properties
    val propsFile = file("gradle.properties")
    if (propsFile.exists()) {
        val props = propsFile.readLines()
            .mapNotNull { line ->
                val parts = line.split("=", limit = 2)
                if (parts.size == 2 && parts[0].trim() == "VERSION_NAME") {
                    parts[1].trim()
                } else null
            }
        if (props.isNotEmpty()) return props.first()
    }

    throw IllegalStateException(
        "VERSION_NAME not set. Set it via:\n" +
        "1. Environment variable: ORG_GRADLE_PROJECT_VERSION_NAME\n" +
        "2. Command line: -PVERSION_NAME=1.0.0\n" +
        "3. gradle.properties: VERSION_NAME=1.0.0"
    )
}

fun getVersionCode(): Int {
    // 1. CI 环境变量: ORG_GRADLE_PROJECT_VERSION_CODE
    val envValue = System.getenv("ORG_GRADLE_PROJECT_VERSION_CODE")
    if (!envValue.isNullOrEmpty()) return envValue.toIntOrNull()
        ?: throw IllegalStateException("VERSION_CODE must be a valid integer")

    // 2. 命令行参数: -PVERSION_CODE
    if (project.hasProperty("VERSION_CODE")) {
        return (project.property("VERSION_CODE") as String).toIntOrNull()
            ?: throw IllegalStateException("VERSION_CODE must be a valid integer")
    }

    // 3. gradle.properties
    val propsFile = file("gradle.properties")
    if (propsFile.exists()) {
        val props = propsFile.readLines()
            .mapNotNull { line ->
                val parts = line.split("=", limit = 2)
                if (parts.size == 2 && parts[0].trim() == "VERSION_CODE") {
                    parts[1].trim().toIntOrNull()
                } else null
            }
        if (props.isNotEmpty()) return props.first()
    }

    throw IllegalStateException(
        "VERSION_CODE not set. Set it via:\n" +
        "1. Environment variable: ORG_GRADLE_PROJECT_VERSION_CODE\n" +
        "2. Command line: -PVERSION_CODE=123\n" +
        "3. gradle.properties: VERSION_CODE=123"
    )
}

android {
    namespace = "com.example.jitterpay"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.jitterpay"
        minSdk = 24
        targetSdk = 36
        // 版本号从环境变量/命令行/gradle.properties获取
        versionName = getVersionName()
        versionCode = getVersionCode()

        testInstrumentationRunner = "com.example.jitterpay.HiltTestRunner"
    }

    // Product flavors for environment-specific configuration
    flavorDimensions += "environment"

    productFlavors {
        create("dev") {
            dimension = "environment"
            // 开发环境使用本地服务器
            buildConfigField("String", "CDN_BASE_URL", "\"http://10.0.2.2:8080\"")
            // 保留包名后缀以区分 dev/prod 版本
            applicationIdSuffix = ".dev"
            // 开发版本启用明文 HTTP 支持
            manifestPlaceholders["networkSecurityConfig"] = "@xml/network_security_config"
        }
        create("prod") {
            dimension = "environment"
            // 生产环境使用生产服务器
            buildConfigField("String", "CDN_BASE_URL", "\"https://store.flowerwine.dpdns.org\"")
            // 生产版本使用默认网络安全配置（禁用明文 HTTP）
            manifestPlaceholders["networkSecurityConfig"] = "@xml/network_security_config_prod"
        }
    }

    signingConfigs {
        create("release") {
            // 从环境变量读取签名配置，用于 CI/CD
            storeFile = System.getenv("KEYSTORE_FILE")?.let { file(it) }
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "android"
            keyAlias = System.getenv("KEY_ALIAS") ?: "androiddebugkey"
            keyPassword = System.getenv("KEY_PASSWORD") ?: "android"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            isUniversalApk = true 
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        // Suppress warnings for backtick-quoted function names in tests (Kotlin BDD style)
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-Xjvm-default=all"
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE"
            excludes += "/META-INF/LICENSE.txt"
            excludes += "/META-INF/license.txt"
            excludes += "/META-INF/NOTICE"
            excludes += "/META-INF/NOTICE.txt"
            excludes += "/META-INF/notice.txt"
        }
    }

    lint {
        checkTestSources = false
        // Disable invalid identifier warning for Kotlin BDD-style test names with backticks
        // Kotlin officially supports backtick-quoted identifiers for function names
        disable += listOf(
            "InvalidPackageName",
            "InvalidFunctionName"
        )
        abortOnError = false
        warningsAsErrors = false
        checkReleaseBuilds = false
    }
}

// KSP configuration for Hilt
ksp {
    arg("hilt.enableAggregatingTask", "true")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.compose)
    implementation(libs.compose.m3)
    
    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.navigation.compose)
    ksp(libs.androidx.room.compiler)
    
    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.compiler)
    
    // Hilt Testing
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)

    // OkHttp for update checks
    implementation(libs.okhttp)

    // Lottie for splash animation
    implementation(libs.lottie.compose)

    implementation(libs.annotations)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation("org.robolectric:robolectric:4.11.1")
    testImplementation("androidx.test:core:1.5.0")
    testImplementation(libs.androidx.work.testing)
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation(libs.mockwebserver)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}