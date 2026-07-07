plugins {
    alias(kei.plugins.extension)
}

keiyoushi {
    name = "E-Hentai"

    versionCode = 27
    contentWarning = ContentWarning.NSFW
    libVersion = "1.4"

    source {
        name = "E-Hentai"
        lang = "all"
        baseUrl = "https://e-hentai.org"
        skipCodeGen = true
    }

    deeplink {
        host("e-hentai.org")
        path("/g/..*/..*")
    }
}
