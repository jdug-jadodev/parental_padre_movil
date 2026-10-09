plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "dev.controlparental.parent"
    compileSdk = 36

    defaultConfig {
        applicationId = "dev.controlparental.parent"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "0.0.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures { compose = true }
    sourceSets.getByName("test").java.srcDir("src/soporteTest/kotlin")
    sourceSets.getByName("androidTest").java.srcDir("src/soporteTest/kotlin")
}

kotlin {
    jvmToolchain(21)
    compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) }
}

dependencies {
    implementation(project(":core-protocol"))
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.coroutines.core)
    implementation(libs.serialization.json)
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.serialization.json)
    debugImplementation(libs.compose.ui.test.manifest)
}

// Solo en el APK instrumentado, nunca en la app debug/release.
abstract class PrepararVectoresAndroid : DefaultTask() {
    @get:InputFile
    abstract val archivo: RegularFileProperty
    @get:OutputDirectory
    abstract val directorio: DirectoryProperty

    @TaskAction
    fun preparar() {
        val destino = directorio.get().asFile
        destino.mkdirs()
        archivo.get().asFile.copyTo(destino.resolve("vectores_crypto.json"), overwrite = true)
    }
}
val copiarVectoresAndroid by tasks.registering(PrepararVectoresAndroid::class) {
    archivo.set(rootProject.layout.projectDirectory.file("pruebas/vectores_crypto.json"))
    directorio.set(layout.buildDirectory.dir("generated/assets/pruebas"))
}
// La API oficial comunica esta dependencia tanto a empaquetado como a lint.
androidComponents.onVariants { variante ->
    variante.androidTest?.sources?.assets?.addGeneratedSourceDirectory(copiarVectoresAndroid) { it.directorio }
}
