plugins {
    alias(kei.plugins.extension)
}

keiyoushi {
    source {
        name = "東立"
        lang = "zh"
        baseUrl = "https://ebook.tongli.com.tw"
    }
    name = "Tongli"
    versionCode = 1
    contentWarning = ContentWarning.NSFW
    libVersion = "1.4"
}
