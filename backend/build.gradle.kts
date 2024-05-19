plugins {
	java
	id("org.springframework.boot") version "3.2.5"
	id("io.spring.dependency-management") version "1.1.4"
}

group = "io.github.staanov"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	implementation("org.apache.lucene:lucene-core:9.6.0")
	implementation("org.apache.lucene:lucene-facet:9.6.0")
	implementation("ch.qos.logback:logback-classic:1.4.14")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
