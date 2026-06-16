package eu.kanade.tachiyomi.extension.pt.risentoons

import android.content.SharedPreferences
import eu.kanade.tachiyomi.network.POST
import keiyoushi.utils.parseAs
import keiyoushi.utils.toJsonRequestBody
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.Cookie
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException

internal const val LOGIN_REQUIRED_MESSAGE =
    "Configure email e senha nas configuracoes da fonte."

private const val ACCESS_TOKEN_COOKIE = "access_token"
private const val REFRESH_TOKEN_COOKIE = "refresh_token"
private const val SESSION_ID_COOKIE = "session_id"

private val AUTH_COOKIE_NAMES = setOf(
    ACCESS_TOKEN_COOKIE,
    REFRESH_TOKEN_COOKIE,
    SESSION_ID_COOKIE,
)

internal class AuthSessionProvider(
    private val preferences: SharedPreferences,
    private val client: OkHttpClient,
    private val apiUrl: String,
    private val loginHeaders: Headers,
) {
    @Synchronized
    fun requireSession(): AuthSession = savedSession()
        ?: login()
        ?: throw IOException("Falha ao realizar login na Risentoons.")

    @Synchronized
    fun clear() {
        preferences.edit()
            .remove(ACCESS_TOKEN_PREF)
            .remove(REFRESH_TOKEN_PREF)
            .remove(SESSION_ID_PREF)
            .apply()
    }

    private fun savedSession(): AuthSession? {
        val accessToken = preferences.getString(ACCESS_TOKEN_PREF, "").orEmpty()
        val refreshToken = preferences.getString(REFRESH_TOKEN_PREF, "").orEmpty()
        val sessionId = preferences.getString(SESSION_ID_PREF, "").orEmpty()
        if (accessToken.isEmpty() || refreshToken.isEmpty() || sessionId.isEmpty()) return null
        return AuthSession(accessToken, refreshToken, sessionId)
    }

    private fun login(): AuthSession? {
        val email = preferences.getString(EMAIL_PREF, "").orEmpty()
        val password = preferences.getString(PASSWORD_PREF, "").orEmpty()
        if (email.isEmpty() || password.isEmpty()) throw IOException(LOGIN_REQUIRED_MESSAGE)

        return runCatching {
            val body = LoginRequest(
                email = email.trim(),
                password = password,
            ).toJsonRequestBody()

            client.newCall(POST("$apiUrl/auth/login", loginHeaders, body)).execute().use { response ->
                if (!response.isSuccessful) return@use null

                response.parseAs<LoginResponse>()
                    .toAuthSession(response.authCookies())
                    .also(::save)
            }
        }.getOrNull()
    }

    private fun save(session: AuthSession) {
        preferences.edit()
            .putString(ACCESS_TOKEN_PREF, session.accessToken)
            .putString(REFRESH_TOKEN_PREF, session.refreshToken)
            .putString(SESSION_ID_PREF, session.sessionId)
            .apply()
    }

    companion object {
        const val EMAIL_PREF = "email"
        const val PASSWORD_PREF = "password"
        private const val ACCESS_TOKEN_PREF = "access_token"
        private const val REFRESH_TOKEN_PREF = "refresh_token"
        private const val SESSION_ID_PREF = "session_id"
    }
}

internal class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val sessionId: String,
) {
    fun toCookies(): List<Pair<String, String>> = listOf(
        "refresh_token" to refreshToken,
        "session_id" to sessionId,
        "access_token" to accessToken,
    )
}

@Serializable
private class LoginRequest(
    private val email: String,
    private val password: String,
)

@Serializable
private class LoginResponse(
    private val tokens: LoginTokensDto,
    @SerialName("session_nonce") private val sessionNonce: String,
) {
    fun toAuthSession(cookies: Map<String, String>): AuthSession = tokens.toAuthSession(
        accessToken = cookies[ACCESS_TOKEN_COOKIE],
        refreshToken = cookies[REFRESH_TOKEN_COOKIE],
        sessionId = cookies[SESSION_ID_COOKIE] ?: sessionNonce,
    )
}

@Serializable
private class LoginTokensDto(
    @SerialName("access_token") private val accessToken: String,
    @SerialName("refresh_token") private val refreshToken: String,
) {
    fun toAuthSession(
        accessToken: String?,
        refreshToken: String?,
        sessionId: String,
    ): AuthSession = AuthSession(
        accessToken = accessToken ?: this.accessToken,
        refreshToken = refreshToken ?: this.refreshToken,
        sessionId = sessionId,
    )
}

private fun Response.authCookies(): Map<String, String> = headers("Set-Cookie")
    .mapNotNull { Cookie.parse(request.url, it) }
    .filter { it.name in AUTH_COOKIE_NAMES }
    .associate { it.name to it.value }
