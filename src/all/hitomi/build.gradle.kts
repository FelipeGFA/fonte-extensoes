plugins {
    alias(kei.plugins.extension)
}

keiyoushi {
    name = "Hitomi"

    versionCode = 41
    contentWarning = ContentWarning.NSFW
    libVersion = "1.4"

    source {
        name = "Hitomi"
        lang = "all"
        baseUrl = "https://hitomi.la"
        skipCodeGen = true
    }
}
