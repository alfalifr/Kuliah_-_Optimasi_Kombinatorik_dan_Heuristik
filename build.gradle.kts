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

    implementation("sidev.lib.kotlin.multi:StdLib-jvm:0.0.1x")
    implementation("sidev.lib.kotlin.multi:Math-jvm:0.0.1x")
}
