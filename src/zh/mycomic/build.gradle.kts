plugins {
    alias(kei.plugins.extension)
}

keiyoushi {
    source {
        name = "MyComic"
        lang = "zh"
        baseUrl = "https://mycomic.com"
    }
    name = "MyComic"
    versionCode = 4
    contentWarning = ContentWarning.NSFW
    libVersion = "1.4"
}
