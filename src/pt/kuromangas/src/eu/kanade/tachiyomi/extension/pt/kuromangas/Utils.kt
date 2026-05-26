package eu.kanade.tachiyomi.extension.pt.kuromangas

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import java.io.IOException

internal fun requireKuroLogin(): Nothing {
    showKuroToast(LOGIN_REQUIRED_MESSAGE)
    throw IOException(LOGIN_REQUIRED_MESSAGE)
}

internal fun showKuroToast(message: String) {
    Handler(Looper.getMainLooper()).post {
        Toast.makeText(Injekt.get<Application>(), message, Toast.LENGTH_LONG).show()
    }
}

internal const val LOGIN_REQUIRED_MESSAGE = "Configure email e senha nas configuracoes da fonte KuroMangas"
