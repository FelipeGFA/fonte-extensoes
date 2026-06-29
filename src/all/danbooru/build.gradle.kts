plugins {
    alias(kei.plugins.extension)
}

keiyoushi {
    source {
        name = "Danbooru"
        lang = "all"
        baseUrl = "https://danbooru.donmai.us"
    }
    name = "Danbooru"
    versionCode = 3
    contentWarning = ContentWarning.NSFW
    libVersion = "1.4"
}
