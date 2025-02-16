plugins {
    id("java")
    id("org.springframework.boot") version "3.4.2"
    id("io.spring.dependency-management") version "1.1.7"
//	id("org.graalvm.buildtools.native") version "0.10.4"
    id("io.gatling.gradle") version "3.13.3.1"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(23)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}


dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    runtimeOnly("org.postgresql:postgresql")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testImplementation("io.gatling.highcharts:gatling-charts-highcharts:3.13.3")
//    implementation(project.parent?.path ?: )
//    testImplementation(project(":vtweb", "testImplementation"))
//    implementation("org.eclipse.aether:aether-api:1.1.0")
//    implementation("org.eclipse.aether:aether-spi:1.1.0")
//    implementation("org.eclipse.aether:aether-util:1.1.0")
//    implementation("org.eclipse.aether:aether-impl:1.1.0")
//    implementation("org.eclipse.aether:aether-connector-basic:1.1.0")
//    implementation("org.eclipse.aether:aether-transport-http:1.1.0")
//    implementation("org.eclipse.aether:aether-transport-file:1.1.0")
//    implementation("org.apache.maven:maven-aether-provider:3.3.9")
    implementation("org.jboss.shrinkwrap.resolver:shrinkwrap-resolver-depchain:3.3.3")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
