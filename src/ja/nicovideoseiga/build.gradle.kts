plugins {
    alias(kei.plugins.extension)
}

keiyoushi {
    source {
        name = "Nicovideo Seiga"
        lang = "ja"
        baseUrl = "https://sp.manga.nicovideo.jp"
    }
    name = "Nicovideo Seiga"
    versionCode = 8
    contentWarning = ContentWarning.SAFE
    libVersion = "1.4"
}
