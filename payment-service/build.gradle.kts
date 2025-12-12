plugins {
    java
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("org.openapi.generator")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    implementation("org.openapitools:jackson-databind-nullable:0.2.6")
    implementation("io.swagger.core.v3:swagger-annotations:2.2.22")
}

tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("generatePaymentServer") {

    generatorName.set("spring")
    inputSpec.set("$rootDir/openapi/payment-service-api.yaml")

    val outDir = layout.buildDirectory.dir("generated/payment-server")
    outputDir.set(outDir.get().asFile.path)

    apiPackage.set("com.amit.payment.api")
    modelPackage.set("com.amit.payment.model")

    additionalProperties.set(
        mapOf(
            "interfaceOnly" to "true",
            "reactive" to "true",
            "useSpringBoot3" to "true",
            "useTags" to "true"
        )
    )
}

sourceSets["main"].java.srcDir(
    layout.buildDirectory.dir("generated/payment-server/src/main/java")
)

tasks.named("compileJava") {
    dependsOn("generatePaymentServer")
}