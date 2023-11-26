import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.1.1"
	id("io.spring.dependency-management") version "1.1.0"
	kotlin("jvm") version "1.8.22"
	kotlin("plugin.spring") version "1.8.22"
}

group = "pl.kawaleria"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
	mavenCentral()
}

val testcontainersVersion = "1.18.3"
val assertJVersion = "3.24.2"
val thumbnailatorVersion = "0.4.20"

dependencies {
 	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb")

	implementation("org.springframework.security:spring-security-oauth2-jose")
	implementation("org.springframework.security:spring-security-oauth2-resource-server")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0")
	implementation("io.micrometer:micrometer-registry-prometheus:1.12.0")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("net.coobird:thumbnailator:$thumbnailatorVersion")

//	implementation("io.minio:minio:8.5.4")
//	implementation("org.springframework.kafka:spring-kafka")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

//	mongo migrations
	implementation("com.github.cloudyrock.mongock:mongock-bom:4.3.8")
	implementation("com.github.cloudyrock.mongock:mongodb-springdata-v3-driver:4.3.8")
	implementation("com.github.cloudyrock.mongock:mongock-spring-v5:4.3.8")

//	tests
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("org.springframework.kafka:spring-kafka-test")
	testImplementation("org.testcontainers:junit-jupiter:$testcontainersVersion")
	implementation("com.redis.testcontainers:testcontainers-redis:1.6.4")
	testImplementation("org.testcontainers:mongodb:$testcontainersVersion")
	testImplementation("org.assertj:assertj-core:$assertJVersion")
}

dependencyManagement {
	imports {
		mavenBom("org.testcontainers:testcontainers-bom:$testcontainersVersion")
	}
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs += "-Xjsr305=strict"
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
