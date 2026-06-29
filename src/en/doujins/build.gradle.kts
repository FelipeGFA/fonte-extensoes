plugins {
    alias(kei.plugins.extension)
}

keiyoushi {
    source {
        name = "Doujins"
        lang = "en"
        baseUrl = "https://doujins.com"
    }
    name = "Doujins"
    versionCode = 6
    contentWarning = ContentWarning.NSFW
    libVersion = "1.4"
}
