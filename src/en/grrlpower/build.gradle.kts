plugins {
    alias(kei.plugins.extension)
}

keiyoushi {
    source {
        name = "Grrl Power Comic"
        lang = "en"
        baseUrl = "https://www.grrlpowercomic.com"
    }
    name = "Grrl Power Comic"
    versionCode = 4
    contentWarning = ContentWarning.SAFE
    libVersion = "1.4"
}

dependencies {

    implementation(project(":lib:textinterceptor"))
}
