plugins {
    id("java")
}

group = "frankvicky.cc"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.netty:netty-all:4.1.108.Final")
    implementation("ch.qos.logback:logback-classic:1.5.3")
    implementation("ch.qos.logback:logback-core:1.5.3")
    implementation("org.slf4j:slf4j-api:2.0.12")

    implementation(platform("org.junit:junit-bom:5.10.0"))
    implementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}