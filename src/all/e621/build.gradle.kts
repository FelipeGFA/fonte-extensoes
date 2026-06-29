plugins {
    alias(kei.plugins.extension)
}

keiyoushi {
    source {
        name = "e621"
        lang = "all"
        baseUrl = "https://e621.net"
    }
    name = "e621"
    versionCode = 2
    contentWarning = ContentWarning.NSFW
    libVersion = "1.4"
}
