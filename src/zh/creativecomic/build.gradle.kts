plugins {
    alias(kei.plugins.extension)
}

keiyoushi {
    source {
        name = "CCC追漫台"
        lang = "zh-Hant"
        baseUrl = "https://www.creative-comic.tw"
    }
    name = "Creative Comic Collection"
    versionCode = 2
    contentWarning = ContentWarning.SAFE
    libVersion = "1.4"
}

dependencies {

    implementation(project(":lib:cryptoaes"))
}
