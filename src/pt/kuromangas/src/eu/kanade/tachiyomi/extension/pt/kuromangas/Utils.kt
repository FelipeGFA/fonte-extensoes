package eu.kanade.tachiyomi.extension.pt.kuromangas

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import java.io.IOException

internal fun requireKuroLogin(): Nothing {
    Handler(Looper.getMainLooper()).post {
        Toast.makeText(Injekt.get<Application>(), LOGIN_REQUIRED_MESSAGE, Toast.LENGTH_LONG).show()
    }
    throw IOException(LOGIN_REQUIRED_MESSAGE)
}

private const val LOGIN_REQUIRED_MESSAGE = "Faca login na WebView da KuroMangas e tente novamente"
