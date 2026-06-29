plugins {
    alias(kei.plugins.extension)
}

keiyoushi {
    source {
        name = "Hentai2Read"
        lang = "en"
        baseUrl = "https://hentai2read.com"
    }
    name = "Hentai2Read"
    versionCode = 19
    contentWarning = ContentWarning.NSFW
    libVersion = "1.4"

    deeplink {
        host("hentai2read.com")
        path("/..*")
    }
}
