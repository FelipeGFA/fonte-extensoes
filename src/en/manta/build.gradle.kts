plugins {
    alias(kei.plugins.extension)
}

keiyoushi {
    source {
        name = "Manta"
        lang = "en"
        baseUrl = "https://manta.net"
    }
    name = "Manta Comics"
    versionCode = 6
    contentWarning = ContentWarning.NSFW
    libVersion = "1.4"
}
