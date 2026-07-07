plugins {
    alias(kei.plugins.extension)
}

keiyoushi {
    name = "NHentai"

    versionCode = 55
    contentWarning = ContentWarning.NSFW
    libVersion = "1.4"

    source {
        name = "NHentai"
        lang = "all"
        baseUrl = "https://nhentai.net"
        skipCodeGen = true
    }

    deeplink {
        host("nhentai.net")
        path("/g/..*")
    }
}

dependencies {
    implementation(project(":lib:randomua"))
}
