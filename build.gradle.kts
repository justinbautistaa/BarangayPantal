import java.io.File

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}

val localBuildRoot = File(
    System.getenv("LOCALAPPDATA") ?: rootDir.absolutePath,
    "BarangayPantalBuild/${rootProject.name}"
)

layout.buildDirectory.set(localBuildRoot.resolve(name))

subprojects {
    layout.buildDirectory.set(localBuildRoot.resolve(name))
}
