plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.bouncycastle:bcpkix-jdk18on:1.78.1")
    implementation("org.bouncycastle:bcprov-jdk18on:1.78.1")
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<JavaExec>("runCa") {
    group = "application"
    mainClass.set("server.CaGenerator")
    classpath = sourceSets.main.get().runtimeClasspath
    
}

tasks.register<JavaExec>("runServer") {
    group = "application"
    mainClass.set("server.Server")
    classpath = sourceSets.main.get().runtimeClasspath
    
}

tasks.register<JavaExec>("runClient") {
    group = "application"
    mainClass.set("client.Client")
    classpath = sourceSets.main.get().runtimeClasspath
    
}
