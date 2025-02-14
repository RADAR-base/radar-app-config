plugins {
    id("kotlin-convention")
    id("org.radarbase.radar-publishing")
}

radarPublishing {
    val githubRepoName = "RADAR-base/radar-app-config"
    githubUrl.set("https://github.com/$githubRepoName.git")

    developers {
        developer {
            id.set("pvannierop")
            name.set("Pim van Nierop")
            email.set("pim@thehyve.nl")
            organization.set("The Hyve")
        }
    }
}
