plugins {
    id("com.android.library")
}

android {
    namespace = "eu.darken.porter.manager.protocol"
    buildFeatures {
        buildConfig = false
        aidl = true
    }
    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
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
    // IPorterManager hands out the protocol's IPorterRemoteProcess.
    api(project(":protocol"))
}
