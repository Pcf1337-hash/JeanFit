package com.jeanfit.app.domain.update

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.core.content.FileProvider
import com.jeanfit.app.BuildConfig
import com.jeanfit.app.data.api.GithubApi
import com.jeanfit.app.data.api.GithubRelease
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

private const val PREFS_NAME          = "jeanfit_update_prefs"
private const val KEY_LAST_CHECK      = "last_update_check"
private const val KEY_SKIPPED_VERSION = "skipped_version"
private const val COOLDOWN_MS         = 24L * 60 * 60 * 1000  // 24 Stunden

data class ReleaseInfo(
    val versionName: String,     // z.B. "1.2.0"
    val versionCode: Int,        // z.B. 3
    val tagName: String,         // z.B. "v1.2.0"
    val title: String,
    val changelog: String,
    val publishedAt: String,
    val downloadUrl: String,
    val isForced: Boolean        // FORCED_UPDATE: true im Changelog
)

sealed class DownloadState {
    data object Idle : DownloadState()
    data class Downloading(val progressPercent: Int) : DownloadState()
    data class ReadyToInstall(val apkFile: File) : DownloadState()
    data class Error(val message: String) : DownloadState()
}

@Singleton
class AppUpdateManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val githubApi: GithubApi,
    private val okHttpClient: OkHttpClient
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ───────────────── Version-Vergleich ─────────────────

    fun getCurrentVersionCode(): Int = BuildConfig.VERSION_CODE
    fun getCurrentVersionName(): String = BuildConfig.VERSION_NAME

    private fun parseVersionCode(tagName: String): Int {
        // "v1.2.0" → 120, "v2.0.1" → 201
        return try {
            val clean = tagName.trimStart('v', 'V')
            val parts = clean.split(".")
            val major = parts.getOrNull(0)?.toIntOrNull() ?: 0
            val minor = parts.getOrNull(1)?.toIntOrNull() ?: 0
            val patch = parts.getOrNull(2)?.toIntOrNull() ?: 0
            major * 10000 + minor * 100 + patch
        } catch (e: Exception) { 0 }
    }

    // ───────────────── Cooldown ─────────────────

    fun shouldCheckForUpdate(): Boolean {
        val lastCheck = prefs.getLong(KEY_LAST_CHECK, 0L)
        return System.currentTimeMillis() - lastCheck > COOLDOWN_MS
    }

    private fun saveLastCheck() {
        prefs.edit().putLong(KEY_LAST_CHECK, System.currentTimeMillis()).apply()
    }

    fun getLastCheckTimestamp(): Long = prefs.getLong(KEY_LAST_CHECK, 0L)

    fun formatLastCheck(): String {
        val ts = getLastCheckTimestamp()
        if (ts == 0L) return "Noch nie"
        val diff = System.currentTimeMillis() - ts
        val minutes = diff / 60_000
        val hours   = diff / 3_600_000
        val days    = diff / 86_400_000
        return when {
            minutes < 1  -> "Gerade eben"
            minutes < 60 -> "vor $minutes Minute${if (minutes != 1L) "n" else ""}"
            hours < 24   -> "vor $hours Stunde${if (hours != 1L) "n" else ""}"
            else         -> "vor $days Tag${if (days != 1L) "en" else ""}"
        }
    }

    // ───────────────── Version überspringen ─────────────────

    fun skipVersion(version: String) {
        prefs.edit().putString(KEY_SKIPPED_VERSION, version).apply()
    }

    fun isVersionSkipped(version: String): Boolean =
        prefs.getString(KEY_SKIPPED_VERSION, null) == version

    // ───────────────── GitHub API ─────────────────

    suspend fun fetchLatestRelease(): ReleaseInfo? = withContext(Dispatchers.IO) {
        try {
            val response = githubApi.getLatestRelease(BuildConfig.GITHUB_RELEASES_URL)
            saveLastCheck()
            if (!response.isSuccessful) return@withContext null
            response.body()?.toReleaseInfo()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun checkForUpdate(): Pair<Boolean, ReleaseInfo?> {
        val release = fetchLatestRelease() ?: return false to null
        // Compare using same semantic-version scheme, not the integer versionCode
        val currentParsed = parseVersionCode("v${BuildConfig.VERSION_NAME}")
        val hasUpdate = parseVersionCode(release.tagName) > currentParsed
        return hasUpdate to (if (hasUpdate) release else null)
    }

    // ───────────────── Download ─────────────────

    fun downloadApk(url: String): Flow<DownloadState> = flow {
        emit(DownloadState.Downloading(0))
        val destFile = File(context.cacheDir, "jeanfit-update.apk")
        if (destFile.exists()) destFile.delete()

        try {
            val request = Request.Builder()
                .url(url)
                .header("Accept", "application/octet-stream")
                .build()

            val response = withContext(Dispatchers.IO) {
                okHttpClient.newCall(request).execute()
            }

            if (!response.isSuccessful) {
                emit(DownloadState.Error("HTTP ${response.code}"))
                return@flow
            }

            val body = response.body ?: run {
                emit(DownloadState.Error("Leere Antwort vom Server"))
                return@flow
            }

            val totalBytes = body.contentLength()

            withContext(Dispatchers.IO) {
                body.byteStream().use { input ->
                    destFile.outputStream().use { output ->
                        val buffer = ByteArray(8 * 1024)
                        var bytesRead = 0L
                        var read: Int
                        while (input.read(buffer).also { read = it } != -1) {
                            output.write(buffer, 0, read)
                            bytesRead += read
                            if (totalBytes > 0) {
                                val progress = (bytesRead * 100 / totalBytes).toInt()
                                // Emit alle 5% — Flow in coroutine context
                            }
                        }
                    }
                }
            }
            emit(DownloadState.ReadyToInstall(destFile))
        } catch (e: Exception) {
            destFile.delete()
            emit(DownloadState.Error(e.message ?: "Unbekannter Fehler"))
        }
    }

    /**
     * Startet Download mit Progress-Updates via Callback.
     * Gibt den heruntergeladenen File zurück oder wirft eine Exception.
     */
    suspend fun downloadApkWithProgress(
        url: String,
        onProgress: (Int) -> Unit
    ): File = withContext(Dispatchers.IO) {
        val destFile = File(context.cacheDir, "jeanfit-update.apk")
        if (destFile.exists()) destFile.delete()

        val request = Request.Builder()
            .url(url)
            .header("Accept", "application/octet-stream")
            .build()

        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) throw Exception("HTTP ${response.code}")

        val body = response.body ?: throw Exception("Leere Server-Antwort")
        val totalBytes = body.contentLength()

        body.byteStream().use { input ->
            destFile.outputStream().use { output ->
                val buffer = ByteArray(8 * 1024)
                var bytesRead = 0L
                var read: Int
                var lastReported = -1
                while (input.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                    bytesRead += read
                    if (totalBytes > 0) {
                        val progress = (bytesRead * 100 / totalBytes).toInt()
                        if (progress != lastReported) {
                            lastReported = progress
                            onProgress(progress)
                        }
                    }
                }
            }
        }
        destFile
    }

    // ───────────────── Install ─────────────────

    fun installApk(apkFile: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    // ───────────────── Mapping ─────────────────

    private fun GithubRelease.toReleaseInfo(): ReleaseInfo {
        val changelog = body ?: ""
        val isForced = Regex("FORCED_UPDATE:\\s*true", RegexOption.IGNORE_CASE).containsMatchIn(changelog)
        val apk = assets.firstOrNull { it.name.endsWith(".apk") }
        return ReleaseInfo(
            versionName  = tagName.trimStart('v', 'V'),
            versionCode  = parseVersionCode(tagName),
            tagName      = tagName,
            title        = name,
            changelog    = changelog,
            publishedAt  = publishedAt,
            downloadUrl  = apk?.browserDownloadUrl ?: "",
            isForced     = isForced
        )
    }
}
