plugins {
    alias(kei.plugins.extension)
}

keiyoushi {
    source {
        name = "TOPTOON頂通"
        lang = "zh"
        baseUrl = "https://www.toptoon.net"
    }
    name = "Toptoon.net"
    versionCode = 1
    contentWarning = ContentWarning.NSFW
    libVersion = "1.4"
}
