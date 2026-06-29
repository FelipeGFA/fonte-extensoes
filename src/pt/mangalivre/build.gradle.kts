plugins {
    alias(kei.plugins.extension)
}

keiyoushi {
    source {
        name = "Manga Livre"
        lang = "pt-BR"
        baseUrl = "https://toonlivre.net"
    }
    name = "Manga Livre"
    versionCode = 67
    contentWarning = ContentWarning.SAFE
    libVersion = "1.4"
}
