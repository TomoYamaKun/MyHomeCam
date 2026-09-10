//app/src/main/java/com/myhomecam/papa/AppLogger.kt
//ver 1.02-12

package com.myhomecam.papa

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AppLogger {

    private const val LOG_DIRECTORY = "myhomecam_logs"
    private const val LOG_FILE_NAME = "app.log"
    private const val CRASH_FILE_NAME = "crash.log"

    private lateinit var appContext: Context

    fun initialize(context: Context) {
        appContext =
            context.applicationContext

        installCrashHandler()

        write(
            "SYSTEM",
            "MyHomeCam ログシステムを初期化しました。"
        )
    }

    fun info(
        tag: String,
        message: String
    ) {
        write(
            tag,
            message
        )
    }

    fun error(
        tag: String,
        message: String,
        throwable: Throwable? = null
    ) {
        val detail =
            if (throwable == null) {
                message
            } else {
                buildString {
                    append(message)
                    append("\n")
                    append(
                        throwable.stackTraceToString()
                    )
                }
            }

        write(
            "ERROR/$tag",
            detail
        )
    }

    fun getLogText(): String {
        if (!::appContext.isInitialized) {
            return "ログシステム未初期化"
        }

        val file =
            getLogFile()

        if (!file.exists()) {
            return "ログはありません。"
        }

        return try {
            file.readText()
        } catch (e: Exception) {
            "ログ読み込み失敗: ${e.message}"
        }
    }

    fun getCrashLogText(): String {
        if (!::appContext.isInitialized) {
            return "ログシステム未初期化"
        }

        val file =
            getCrashFile()

        if (!file.exists()) {
            return "クラッシュログはありません。"
        }

        return try {
            file.readText()
        } catch (e: Exception) {
            "クラッシュログ読み込み失敗: ${e.message}"
        }
    }

    fun clearLogs() {
        if (!::appContext.isInitialized) {
            return
        }

        try {
            getLogFile().delete()
            getCrashFile().delete()

            write(
                "SYSTEM",
                "ログを初期化しました。"
            )
        } catch (_: Exception) {
        }
    }

    fun getLogFilePath(): String {
        if (!::appContext.isInitialized) {
            return ""
        }

        return getLogFile().absolutePath
    }

    private fun write(
        tag: String,
        message: String
    ) {
        if (!::appContext.isInitialized) {
            return
        }

        try {
            val file =
                getLogFile()

            file.parentFile?.mkdirs()

            val time =
                SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss.SSS",
                    Locale.JAPAN
                ).format(Date())

            val text =
                "[$time][$tag] $message\n"

            file.appendText(text)
        } catch (_: Exception) {
        }
    }

    private fun getLogFile(): File {
        return File(
            appContext.filesDir,
            "$LOG_DIRECTORY/$LOG_FILE_NAME"
        )
    }

    private fun getCrashFile(): File {
        return File(
            appContext.filesDir,
            "$LOG_DIRECTORY/$CRASH_FILE_NAME"
        )
    }

    private fun installCrashHandler() {
        val defaultHandler =
            Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler {
                thread,
                throwable ->

            writeCrash(
                thread,
                throwable
            )

            defaultHandler?.uncaughtException(
                thread,
                throwable
            )
        }
    }

    private fun writeCrash(
        thread: Thread,
        throwable: Throwable
    ) {
        try {
            val file =
                getCrashFile()

            file.parentFile?.mkdirs()

            val time =
                SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss.SSS",
                    Locale.JAPAN
                ).format(Date())

            val text =
                buildString {
                    append("========== CRASH ==========\n")
                    append("time=$time\n")
                    append("thread=${thread.name}\n")
                    append("\n")
                    append(
                        throwable.stackTraceToString()
                    )
                    append("\n")
                    append("============================\n")
                }

            file.appendText(text)
        } catch (_: Exception) {
        }
    }
}