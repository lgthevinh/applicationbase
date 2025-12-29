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