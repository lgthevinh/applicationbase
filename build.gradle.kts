plugins {
    id("java")
}

group = "org.thingai"
version = ""

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("org.xerial:sqlite-jdbc:3.43.2.0")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.slf4j:slf4j-api:2.0.9") // Logging interface of jdbc

    implementation("com.fasterxml.jackson.core:jackson-core:2.17.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.17.2")

    implementation("com.guardsquare:proguard-gradle:7.8.1")
    implementation("org.apache.logging.log4j:log4j-core:2.24.3")
}

tasks.test {
    useJUnitPlatform()
}

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("com.guardsquare:proguard-gradle:7.8.1")
    }
}

tasks.register<JavaExec>("proguard") {
    mainClass.set("proguard.ProGuard")
    classpath = configurations.compileClasspath.get()
    args = listOf("@proguard-rules.pro")
}