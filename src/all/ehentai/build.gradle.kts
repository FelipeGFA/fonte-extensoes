plugins {
    alias(kei.plugins.extension)
}

keiyoushi {
    source {
        name = "E-Hentai"
        lang = "ja"
        baseUrl = "https://e-hentai.org"
    }
    source {
        name = "E-Hentai"
        lang = "en"
        baseUrl = "https://e-hentai.org"
    }
    source {
        name = "E-Hentai"
        lang = "zh"
        baseUrl = "https://e-hentai.org"
    }
    source {
        name = "E-Hentai"
        lang = "nl"
        baseUrl = "https://e-hentai.org"
    }
    source {
        name = "E-Hentai"
        lang = "fr"
        baseUrl = "https://e-hentai.org"
    }
    source {
        name = "E-Hentai"
        lang = "de"
        baseUrl = "https://e-hentai.org"
    }
    source {
        name = "E-Hentai"
        lang = "hu"
        baseUrl = "https://e-hentai.org"
    }
    source {
        name = "E-Hentai"
        lang = "it"
        baseUrl = "https://e-hentai.org"
    }
    source {
        name = "E-Hentai"
        lang = "ko"
        baseUrl = "https://e-hentai.org"
    }
    source {
        name = "E-Hentai"
        lang = "pl"
        baseUrl = "https://e-hentai.org"
    }
    source {
        name = "E-Hentai"
        lang = "pt-BR"
        baseUrl = "https://e-hentai.org"
        id = 7151438547982231541L
    }
    source {
        name = "E-Hentai"
        lang = "ru"
        baseUrl = "https://e-hentai.org"
    }
    source {
        name = "E-Hentai"
        lang = "es"
        baseUrl = "https://e-hentai.org"
    }
    source {
        name = "E-Hentai"
        lang = "th"
        baseUrl = "https://e-hentai.org"
    }
    source {
        name = "E-Hentai"
        lang = "vi"
        baseUrl = "https://e-hentai.org"
    }
    source {
        name = "E-Hentai"
        lang = "none"
        baseUrl = "https://e-hentai.org"
    }
    source {
        name = "E-Hentai"
        lang = "other"
        baseUrl = "https://e-hentai.org"
    }

    name = "E-Hentai"

    versionCode = 27
    contentWarning = ContentWarning.NSFW
    libVersion = "1.4"

    deeplink {
        host("e-hentai.org")
        path("/g/..*/..*")
    }
}
