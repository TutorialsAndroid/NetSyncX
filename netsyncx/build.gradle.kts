plugins {
    alias(libs.plugins.android.library)
}

extra.apply {
    set("PUBLISH_GROUP_ID", "io.github.tutorialsandroid")
    set("PUBLISH_VERSION", "1.0.0")
    set("PUBLISH_ARTIFACT_ID", "netsyncx")

    set("PUBLISH_DESCRIPTION", "Smart Android network monitoring and offline retry toolkit for Java and Kotlin apps.")
    set("PUBLISH_URL", "https://github.com/tutorialsandroid/netsyncx")

    set("PUBLISH_LICENSE_NAME", "Apache License")
    set("PUBLISH_LICENSE_URL", "https://github.com/tutorialsandroid/netsyncx/blob/main/LICENSE")

    set("PUBLISH_DEVELOPER_ID", "tutorialsandroid")
    set("PUBLISH_DEVELOPER_NAME", "Akshay Masram")
    set("PUBLISH_DEVELOPER_EMAIL", "akshaysunilmasram@yahoo.com")

    set("PUBLISH_SCM_CONNECTION", "scm:git:github.com/tutorialsandroid/netsyncx.git")
    set("PUBLISH_SCM_DEVELOPER_CONNECTION", "scm:git:ssh://github.com/tutorialsandroid/netsyncx.git")
    set("PUBLISH_SCM_URL", "https://github.com/tutorialsandroid/netsyncx/tree/main")
}

android {
    namespace = "io.tutorialsandroid.netsyncx"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}
apply(from = "${rootProject.projectDir}/scripts/publish-module.gradle")