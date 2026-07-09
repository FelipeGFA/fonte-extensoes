plugins {
    alias(kei.plugins.extension)
}

keiyoushi {
    source {
        name = "NHentai"
        lang = "en"
        baseUrl = "https://nhentai.net"
    }
    source {
        name = "NHentai"
        lang = "ja"
        baseUrl = "https://nhentai.net"
    }
    source {
        name = "NHentai"
        lang = "zh"
        baseUrl = "https://nhentai.net"
    }
    source {
        name = "NHentai"
        lang = "all"
        baseUrl = "https://nhentai.net"
        id = 7309872737163460316
    }

    name = "NHentai"

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
