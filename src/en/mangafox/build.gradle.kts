plugins {
    alias(kei.plugins.extension)
}

keiyoushi {
    source {
        name = "MangaFox"
        lang = "en"
        baseUrl = "https://fanfox.net"
    }
    name = "MangaFox"
    versionCode = 9
    contentWarning = ContentWarning.NSFW
    libVersion = "1.4"
}
