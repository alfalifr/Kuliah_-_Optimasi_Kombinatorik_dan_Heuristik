import java.net.URI

plugins {
    java
    kotlin("jvm") version "1.4.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven{ url = URI("https://dl.bintray.com/alfalifr/SidevLib") }
}

dependencies {
    implementation(kotlin("stdlib"))
    testCompile("junit", "junit", "4.12")

    implementation("sidev.lib.kotlin:KtStdLib-jvm:0.0.1x.11112020")
    implementation("sidev.lib.kotlin:KtMath-jvm:0.0.1x")
    implementation("sidev.lib.kotlin:KtAsync-jvm:0.0.1x")
    implementation("sidev.lib.kotlin:KtReflex-jvm:0.0.1x")
    implementation("sidev.lib.jvm:JvmStdLib:0.0.1xx")
}
