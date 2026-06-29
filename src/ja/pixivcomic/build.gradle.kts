plugins {
    alias(kei.plugins.extension)
}

keiyoushi {
    source {
        name = "Pixivコミック"
        lang = "ja"
        baseUrl = "https://comic.pixiv.net"
    }
    name = "Pixiv Comic"
    versionCode = 3
    contentWarning = ContentWarning.SAFE
    libVersion = "1.4"
}
