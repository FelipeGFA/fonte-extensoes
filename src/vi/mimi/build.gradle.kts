plugins {
    alias(kei.plugins.extension)
}

keiyoushi {
    source {
        name = "MiMi"
        lang = "vi"
        baseUrl = "https://mimimoe.moe"
    }
    name = "MiMi"
    versionCode = 6
    contentWarning = ContentWarning.NSFW
    libVersion = "1.4"

    deeplink {
        host("mimimoe.moe")
        path("/manga/..*")
    }
}
