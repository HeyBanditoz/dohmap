plugins {
    java
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
    id("org.flywaydb.flyway") version "10.6.0"
}

group = "io.banditoz"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf:3.2.0")
    implementation("org.springframework.boot:spring-boot-starter-web:3.2.0")
    implementation("org.springframework.boot:spring-boot-starter-actuator:3.2.0")
    implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.3")
    implementation("org.seleniumhq.selenium:selenium-java:4.15.0")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("com.github.f4b6a3:uuid-creator:5.3.3")
    implementation("commons-io:commons-io:2.15.1")
    developmentOnly("org.springframework.boot:spring-boot-devtools:3.2.0")
    runtimeOnly("org.postgresql:postgresql:42.7.1")
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.2.0")
}

buildscript {
    dependencies {
        classpath("org.flywaydb:flyway-database-postgresql:10.6.0")
        classpath("org.postgresql:postgresql:42.7.1")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
