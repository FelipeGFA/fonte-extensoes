plugins {
    alias(kei.plugins.extension)
}

keiyoushi {
    source {
        name = "Hattori Manga"
        lang = "tr"
        baseUrl = "https://hattorimanga.net"
    }
    name = "Hattori Manga"
    versionCode = 44
    contentWarning = ContentWarning.NSFW
    libVersion = "1.4"

    deeplink {
        host("hattorimanga.net")
        path("/manga/..*")
    }
}
