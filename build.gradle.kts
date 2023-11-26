import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.31"
}

group = "com.discord.sapokr.litewaffle"
version = "1.1"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.6.0")
    implementation("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")
    implementation("club.minnced:discord-webhooks:0.7.5")
    implementation("net.dv8tion:JDA:5.0.0-alpha.4") {
        exclude("opus-java")
    }
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_16.toString()
    }
}