// Root-level build.gradle.kts

plugins {
    id("com.android.application") version "8.5.0" apply false
    id("com.android.library") version "8.5.0" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}

task("clean", type = Delete::class) {
    delete(rootProject.buildDir)
}
