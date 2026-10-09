plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvmToolchain(21)
    compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

sourceSets {
    main {
        resources.srcDir("../compartido")
        resources.include("protocolo.schema.json")
    }
    test {
        resources.srcDir("../compartido/ejemplos")
        resources.srcDir("../pruebas")
        resources.include("*.json")
    }
}

dependencies {
    implementation(libs.serialization.json)
    testImplementation(libs.junit)
}
