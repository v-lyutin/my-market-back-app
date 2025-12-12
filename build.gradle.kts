plugins {
	java
	id("org.springframework.boot") version "3.5.7" apply false
	id("io.spring.dependency-management") version "1.1.7" apply false
	id("org.openapi.generator") version "7.8.0" apply false
}

group = "com.amit"
version = "0.0.1"

subprojects {

	apply(plugin = "java")
	apply(plugin = "io.spring.dependency-management")

	repositories {
		mavenCentral()
	}

	java {
		toolchain {
			languageVersion = JavaLanguageVersion.of(21)
		}
	}

	tasks.withType<Test> {
		useJUnitPlatform()
	}

}