plugins {
    alias(kei.plugins.extension)
}

keiyoushi {
    name = "Hentai20"
    versionCode = 1
    contentWarning = ContentWarning.NSFW
    theme = "mangathemesia"
    libVersion = "1.4"

    source {
        lang = "en"
        baseUrl = "https://hentai20.io"
    }
}
