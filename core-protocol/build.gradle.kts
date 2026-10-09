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

// El test puro JVM no exige Node. La interoperabilidad se ejecuta explícitamente.
tasks.test { exclude("**/InteroperabilidadTest.class") }

tasks.register<Test>("testInterop") {
    description = "Verifica firmas nuevas entre JVM y Node sin guardar claves privadas"
    group = "verification"
    dependsOn(tasks.testClasses)
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    include("**/InteroperabilidadTest.class")
    systemProperty("raiz.proyecto", rootProject.projectDir.absolutePath)
}
