plugins {
    alias(kei.plugins.extension)
}

keiyoushi {
    name = "NHentai"
    className = "NHFactory"
    versionCode = 55
    contentWarning = ContentWarning.NSFW
    libVersion = "1.4"

    deeplink {
        host("nhentai.net")
        path("/g/..*")
    }
}

dependencies {
    implementation(project(":lib:randomua"))
}
