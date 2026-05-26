package eu.kanade.tachiyomi.extension.pt.kuromangas

import android.content.SharedPreferences
import eu.kanade.tachiyomi.network.POST
import keiyoushi.utils.JSON_MEDIA_TYPE
import keiyoushi.utils.parseAs
import keiyoushi.utils.toJsonString
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

internal class AuthInterceptor(
    private val tokenProvider: AuthTokenProvider,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (!request.isKuroApiRequest() || request.isLoginRequest() || request.header("Authorization") != null) {
            return chain.proceed(request)
        }

        val token = tokenProvider.currentToken()
        val authenticatedRequest = if (token.isNotBlank()) request.withAuthorization(token) else request
        val response = chain.proceed(authenticatedRequest)

        if (response.code != 401) {
            return response
        }

        response.close()
        tokenProvider.clear()
        throw IOException(LOGIN_REQUIRED_MESSAGE)
    }

    private fun Request.withAuthorization(token: String): Request = newBuilder()
        .header("Authorization", "Bearer $token")
        .build()

    private fun Request.isKuroApiRequest(): Boolean = url.host == KURO_HOST && url.encodedPath.startsWith("/api/")

    private fun Request.isLoginRequest(): Boolean = url.encodedPath == "/api/auth/login"

    private companion object {
        const val KURO_HOST = "kuromangas.com"
    }
}

internal class AuthTokenProvider(
    private val preferences: SharedPreferences,
    private val client: OkHttpClient,
    private val apiUrl: String,
    private val loginHeaders: Headers,
) {

    @Synchronized
    fun currentToken(): String {
        val savedToken = preferences.getString(TOKEN_PREF, "").orEmpty()
        if (savedToken.isNotEmpty()) return savedToken

        val email = preferences.getString(EMAIL_PREF, "").orEmpty()
        val password = preferences.getString(PASSWORD_PREF, "").orEmpty()
        if (email.isEmpty() || password.isEmpty()) return ""

        return login(email, password).token
    }

    @Synchronized
    fun requireToken(): String = currentToken().ifEmpty { throw IOException(LOGIN_REQUIRED_MESSAGE) }

    @Synchronized
    fun clear() {
        preferences.edit().remove(TOKEN_PREF).apply()
    }

    fun checkLogin(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) return

        Thread {
            val result = login(email, password)
            val message = if (result.success) {
                "Login realizado com sucesso"
            } else {
                "Nao foi possivel realizar login"
            }

            showKuroToast(message)
        }.start()
    }

    private fun login(email: String, password: String): LoginResult = runCatching {
        val body = LoginRequest(
            email = email,
            password = password,
            rememberMe = false,
        ).toJsonString().toRequestBody(JSON_MEDIA_TYPE)

        client.newCall(POST("$apiUrl/auth/login", loginHeaders, body)).execute().use { response ->
            if (response.code != 200) {
                return@use LoginResult(success = false)
            }

            val token = response.parseAs<LoginResponse>()
                .tokenOrNull()
                .orEmpty()

            if (token.isNotEmpty()) {
                preferences.edit().putString(TOKEN_PREF, token).apply()
            }

            LoginResult(success = token.isNotEmpty(), token = token)
        }
    }.getOrDefault(LoginResult(success = false))

    private class LoginResult(
        val success: Boolean,
        val token: String = "",
    )

    companion object {
        const val EMAIL_PREF = "pref_email"
        const val PASSWORD_PREF = "pref_password"
        private const val TOKEN_PREF = "pref_token"
    }
}
