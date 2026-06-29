plugins {
    alias(kei.plugins.extension)
}

keiyoushi {
    source {
        name = "MikuDoujin"
        lang = "th"
        baseUrl = "https://miku-doujin.com"
    }
    name = "MikuDoujin"
    versionCode = 7
    contentWarning = ContentWarning.NSFW
    libVersion = "1.4"
}
