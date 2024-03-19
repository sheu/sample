plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("software.amazon.awssdk:sns:2.25.11")
    implementation("org.springframework:spring-beans:6.1.5")
    implementation("org.springframework:spring-web:6.1.5")
    implementation("org.springframework:spring-jdbc:6.1.5")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}