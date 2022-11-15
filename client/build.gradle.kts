/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java application project to get you started.
 * For more details take a look at the 'Building Java & JVM projects' chapter in the Gradle
 * User Manual available at https://docs.gradle.org/7.5.1/userguide/building_java_projects.html
 */

plugins {
    application
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // Use JUnit test framework.
    implementation("org.junit.jupiter:junit-jupiter:5.9.0")

    // ETCD
    implementation("io.etcd:jetcd-core:0.7.3")
    implementation("org.slf4j:slf4j-simple:2.0.3")

    implementation("com.google.code.gson:gson:2.10")

    // https://mvnrepository.com/artifact/io.netty/netty-all
    implementation("io.netty:netty-all:4.1.84.Final")

}

application {
    // Define the main class for the application.
    mainClass.set("it.unibo.ds.lab.consensus.client.ChatClient")
}

tasks.getByName<JavaExec>("run") {
    standardInput = System.`in`
    args("Matteo", "http://localhost:2379")
}