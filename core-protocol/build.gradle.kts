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
tasks.withType<Test>().configureEach {
    systemProperty("raiz.cp2", rootProject.projectDir.absolutePath)
}

// Utilidad exclusivamente JVM test. CREATE_NEW impide regenerar/ocultar un vector previo.
tasks.register<JavaExec>("generarVectorCp2Jvm") {
    group = "verification"
    description = "Publica una única muestra CP2 ficticia, sin claves privadas"
    dependsOn(tasks.testClasses)
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass.set("dev.controlparental.protocol.cp2.GenerarVectorPublicoCp2")
    args(rootProject.file("pruebas/interop_cp2_jvm_publica.json").absolutePath)
}

tasks.register<Test>("testInterop") {
    description = "Verifica firmas nuevas entre JVM y Node sin guardar claves privadas"
    group = "verification"
    dependsOn(tasks.testClasses)
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    include("**/InteroperabilidadTest.class")
    systemProperty("raiz.proyecto", rootProject.projectDir.absolutePath)
    providers.gradleProperty("rutaRelay").orNull?.let { systemProperty("ruta.relay", it) }
}
