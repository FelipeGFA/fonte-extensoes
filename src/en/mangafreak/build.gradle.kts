plugins {
    alias(kei.plugins.extension)
}

keiyoushi {
    source {
        name = "Mangafreak"
        lang = "en"
        baseUrl = "https://ww2.mangafreak.me"
    }
    name = "Mangafreak"
    versionCode = 13
    contentWarning = ContentWarning.NSFW
    libVersion = "1.4"
}
