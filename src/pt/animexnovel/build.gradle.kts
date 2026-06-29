plugins {
    alias(kei.plugins.extension)
}

keiyoushi {
    source {
        name = "AnimeXNovel"
        lang = "pt-BR"
        baseUrl = "https://www.animexnovel.com"
    }
    name = "AnimeXNovel"
    versionCode = 18
    contentWarning = ContentWarning.SAFE
    libVersion = "1.4"
}
