plugins {
    alias(kei.plugins.extension)
}

keiyoushi {
    source {
        name = "嗨皮漫画"
        lang = "zh"
        baseUrl = "https://m.happymh.com"
    }
    name = "Happymh"
    versionCode = 24
    contentWarning = ContentWarning.SAFE
    libVersion = "1.4"
}
