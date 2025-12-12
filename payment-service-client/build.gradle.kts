plugins {
    java
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("org.openapi.generator")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    implementation("jakarta.annotation:jakarta.annotation-api:2.1.1")
    implementation("org.openapitools:jackson-databind-nullable:0.2.6")
    implementation("io.swagger.core.v3:swagger-annotations:2.2.22")
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")
}

tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("generatePaymentClient") {
    generatorName.set("java")
    inputSpec.set("$rootDir/openapi/payment-service-api.yaml")

    val outDir = layout.buildDirectory.dir("generated/payment-client")
    outputDir.set(outDir.get().asFile.path)

    apiPackage.set("com.amit.payment.client.api")
    modelPackage.set("com.amit.payment.client.model")
    invokerPackage.set("com.amit.payment.client.invoker")

    additionalProperties.set(
        mapOf(
            "library" to "webclient",
            "dateLibrary" to "java8",
            "useTags" to "true",
            "useJakartaEe" to "true"
        )
    )
}

sourceSets["main"].java.srcDir(
    layout.buildDirectory.dir("generated/payment-client/src/main/java")
)

tasks.named("compileJava") {
    dependsOn("generatePaymentClient")
}