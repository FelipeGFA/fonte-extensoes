plugins {
    alias(kei.plugins.extension)
}

keiyoushi {
    source {
        name = "Pururin"
        lang = "all"
        baseUrl = "https://pururin.me"
    }
    source {
        name = "Pururin"
        lang = "en"
        baseUrl = "https://pururin.me"
    }
    source {
        name = "Pururin"
        lang = "ja"
        baseUrl = "https://pururin.me"
    }

    name = "Pururin"

    versionCode = 11
    contentWarning = ContentWarning.NSFW
    libVersion = "1.4"
}
