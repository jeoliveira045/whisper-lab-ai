plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
    id("org.springframework.boot") version "4.0.5"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "monokai"
version = "0.0.1-SNAPSHOT"
description = "whisper-api-lab"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

//dependencyManagement {
//    imports {
//        mavenBom("org.springframework.ai:spring-ai-bom:1.0.0")
//    }
//}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("tools.jackson.module:jackson-module-kotlin")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
//    implementation("com.openai:openai-java:0.31.0")
    implementation("org.springframework.ai:spring-ai-starter-model-google-genai")
    implementation("com.google.cloud:libraries-bom:26.32.0")
    // Source: https://mvnrepository.com/artifact/com.google.cloud/google-cloud-speech
    implementation("com.google.cloud:google-cloud-speech:4.78.0")
    // Source: https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-core
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.0")
// Source: https://mvnrepository.com/artifact/org.springframework/spring-test
    implementation("org.springframework:spring-test:4.0.5.RELEASE")
}

extra["springAiVersion"] = "1.1.0"

dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")

    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
