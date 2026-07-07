plugins {
    alias(kei.plugins.extension)
}

keiyoushi {
    name = "Pururin"

    versionCode = 11
    contentWarning = ContentWarning.NSFW
    libVersion = "1.4"

    source {
        name = "Pururin"
        lang = "all"
        baseUrl = "https://pururin.me"
        skipCodeGen = true
    }
}
