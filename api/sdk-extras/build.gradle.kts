plugins {
    id("com.android.library")
}

android {
    namespace = "eu.darken.porter.sdk.extras"
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    buildFeatures {
        buildConfig = false
        aidl = true
    }
    testOptions { unitTests.isIncludeAndroidResources = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

// Pinned here rather than in the root script: the app includes this module directly and never
// applies that script, so the two targets have to agree from this file alone.
kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}

dependencies {
    api(project(":sdk"))

    implementation("androidx.annotation:annotation:1.3.0")

    // The tests build server replies, which are written in the protocol's keys.
    testImplementation(project(":protocol"))
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.16.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")

    // The shell tests stand in for the server, which answers in the protocol's keys.
    androidTestImplementation(project(":protocol"))
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
}

// The tests deliver a fake server through the SDK's internal entry point and reset it between
// cases, so the unit test compilations treat the SDK's compile jar as part of their own module.
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    if (!name.endsWith("UnitTestKotlin")) return@configureEach
    val variant = name.removePrefix("compile").removeSuffix("UnitTestKotlin").replaceFirstChar { it.lowercase() }
    val bundleTask = "bundleLibCompileToJar" + variant.replaceFirstChar { it.uppercase() }
    friendPaths.from(
        project(":sdk").layout.buildDirectory.file(
            "intermediates/compile_library_classes_jar/$variant/$bundleTask/classes.jar",
        ),
    )
}
