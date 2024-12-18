plugins {
    java
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
    id("org.flywaydb.flyway") version "10.6.0"
}

group = "io.banditoz"
version = "0.0.1-SNAPSHOT"
val sbVersion = "3.3.4"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf:${sbVersion}")
    implementation("org.springframework.boot:spring-boot-starter-web:${sbVersion}")
    implementation("org.springframework.boot:spring-boot-starter-security:${sbVersion}")
    implementation("org.springframework.boot:spring-boot-starter-validation:${sbVersion}")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign:4.1.0")
    implementation("org.springframework.boot:spring-boot-starter-actuator:${sbVersion}")
    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6:3.1.2.RELEASE")
    implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.3")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
    implementation("org.mybatis:mybatis-typehandlers-jsr310:1.0.2")
    implementation("com.agido:logback-elasticsearch-appender:3.0.10")
    implementation("org.seleniumhq.selenium:selenium-java:4.25.0")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("com.github.f4b6a3:uuid-creator:5.3.3")
    implementation("commons-io:commons-io:2.15.1")
    implementation("org.jsoup:jsoup:1.17.2")
    implementation("io.micrometer:micrometer-registry-prometheus:1.12.0")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
    developmentOnly("org.springframework.boot:spring-boot-devtools:${sbVersion}")
    runtimeOnly("org.postgresql:postgresql:42.7.4")
    testImplementation("org.springframework.boot:spring-boot-starter-test:${sbVersion}")
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

tasks.getByName<Jar>("jar") {
    enabled = false
}
