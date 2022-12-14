plugins {
    id("com.gradle.enterprise") version "3.11.4"
}

rootProject.name = "lab-consensus"
include("client")
include("presentation")

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
        publishOnFailure()
    }
}
