plugins {
    alias(kei.plugins.extension)
}

keiyoushi {
    source {
        name = "Twi4"
        lang = "ja"
        baseUrl = "https://sai-zen-sen.jp/comics/twi4/"
    }
    name = "Twi4"
    versionCode = 6
    contentWarning = ContentWarning.SAFE
    libVersion = "1.4"

    deeplink {
        host("sai-zen-sen.jp")
        path("/comics/twi4/..*/.*")
    }
}
