//app/src/main/java/com/myhomecam/papa/AppLogger.kt
//ver 1.03-21

package com.myhomecam.papa

import android.content.Context
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.os.Build
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AppLogger {

    private const val TAG = "MyHomeCam"

    private const val LOG_FILE_NAME = "myhomecam.log"

    private const val CRASH_LOG_FILE_NAME =
        "myhomecam_crash.log"

    private const val HEVC_MIME =
        "video/hevc"

    private const val TEST_WIDTH =
        2304

    private const val TEST_HEIGHT =
        2592

    private const val TEST_FRAME_RATE =
        30.0

    private var appContext: Context? = null

    private val lock = Any()

    private var initialized = false

    /**
     * Logger初期化
     */
    fun initialize(context: Context) {

        var shouldInitialize = false

        synchronized(lock) {

            if (!initialized) {

                appContext =
                    context.applicationContext

                initialized = true

                shouldInitialize = true
            }
        }

        if (shouldInitialize) {

            installCrashHandler()

            writeInternal(
                level = "INFO",
                message = "AppLogger initialized"
            )
        }
    }

    /**
     * 通常ログ
     */
    fun info(
        message: String
    ) {

        write(
            level = "INFO",
            message = message
        )
    }

    /**
     * 複数情報対応の通常ログ
     */
    fun info(
        message: String,
        vararg details: Any?
    ) {

        val fullMessage =
            buildMessage(
                message = message,
                details = details
            )

        write(
            level = "INFO",
            message = fullMessage
        )
    }

    /**
     * エラーログ
     */
    fun error(
        message: String
    ) {

        write(
            level = "ERROR",
            message = message
        )
    }

    /**
     * 複数情報対応のエラーログ
     */
    fun error(
        message: String,
        vararg details: Any?
    ) {

        val fullMessage =
            buildMessage(
                message = message,
                details = details
            )

        write(
            level = "ERROR",
            message = fullMessage
        )
    }

    /**
     * 警告ログ
     */
    fun warn(
        message: String
    ) {

        write(
            level = "WARN",
            message = message
        )
    }

    /**
     * デバッグログ
     */
    fun debug(
        message: String
    ) {

        write(
            level = "DEBUG",
            message = message
        )
    }

    /**
     * 複数の値をログ文字列へ変換
     */
    private fun buildMessage(
        message: String,
        details: Array<out Any?>
    ): String {

        if (details.isEmpty()) {
            return message
        }

        val builder =
            StringBuilder()

        builder.append(message)

        details.forEach { detail ->

            builder.append(" | ")

            when (detail) {

                null -> {

                    builder.append(
                        "null"
                    )
                }

                is Throwable -> {

                    builder.append(
                        detail.stackTraceToString()
                    )
                }

                else -> {

                    builder.append(
                        detail.toString()
                    )
                }
            }
        }

        return builder.toString()
    }

    /**
     * ログ出力共通処理
     */
    private fun write(
        level: String,
        message: String
    ) {

        when (level) {

            "ERROR" -> {

                Log.e(
                    TAG,
                    message
                )
            }

            "WARN" -> {

                Log.w(
                    TAG,
                    message
                )
            }

            "DEBUG" -> {

                Log.d(
                    TAG,
                    message
                )
            }

            else -> {

                Log.i(
                    TAG,
                    message
                )
            }
        }

        writeInternal(
            level = level,
            message = message
        )
    }

    /**
     * 通常ログをファイルへ保存
     */
    private fun writeInternal(
        level: String,
        message: String
    ) {

        val context =
            appContext

        if (context == null) {
            return
        }

        val targetContext =
            context

        synchronized(lock) {

            try {

                val file =
                    File(
                        targetContext.filesDir,
                        LOG_FILE_NAME
                    )

                val timestamp =
                    SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss.SSS",
                        Locale.JAPAN
                    ).format(
                        Date()
                    )

                val line =
                    "$timestamp [$level] $message\n"

                file.appendText(
                    text = line,
                    charset = Charsets.UTF_8
                )

            } catch (e: Exception) {

                Log.e(
                    TAG,
                    "ログファイルへの書き込みに失敗",
                    e
                )
            }
        }
    }

    /**
     * LogView互換：
     * 通常ログを全消去
     */
    fun clearLogs() {

        val context =
            appContext

        if (context == null) {
            return
        }

        val file =
            File(
                context.filesDir,
                LOG_FILE_NAME
            )

        try {

            synchronized(lock) {

                file.delete()
            }

        } catch (e: Exception) {

            Log.e(
                TAG,
                "ログ削除に失敗",
                e
            )
        }
    }

    /**
     * LogView互換：
     * 通常ログ全文を取得
     */
    fun getLogText(): String {

        val context =
            appContext

        if (context == null) {
            return "Loggerが初期化されていません。"
        }

        val file =
            File(
                context.filesDir,
                LOG_FILE_NAME
            )

        try {

            val exists =
                file.exists()

            if (!exists) {
                return "ログはありません。"
            }

            val text =
                file.readText(
                    charset = Charsets.UTF_8
                )

            val blank =
                text.isBlank()

            if (blank) {
                return "ログはありません。"
            }

            return text

        } catch (e: Exception) {

            return "ログ読み込み失敗: ${e.message}"
        }
    }

    /**
     * LogView互換：
     * クラッシュログ全文を取得
     */
    fun getCrashLogText(): String {

        val context =
            appContext

        if (context == null) {
            return "Loggerが初期化されていません。"
        }

        val file =
            File(
                context.filesDir,
                CRASH_LOG_FILE_NAME
            )

        try {

            val exists =
                file.exists()

            if (!exists) {
                return "クラッシュログはありません。"
            }

            val text =
                file.readText(
                    charset = Charsets.UTF_8
                )

            val blank =
                text.isBlank()

            if (blank) {
                return "クラッシュログはありません。"
            }

            return text

        } catch (e: Exception) {

            return "クラッシュログ読み込み失敗: ${e.message}"
        }
    }

    /**
     * LogView互換：
     * ログファイルのフルパス
     */
    fun getLogFilePath(): String {

        val context =
            appContext

        if (context == null) {
            return ""
        }

        val file =
            File(
                context.filesDir,
                LOG_FILE_NAME
            )

        return file.absolutePath
    }

    /**
     * 既存コード互換
     */
    fun readLogs(): String {

        return getLogText()
    }

    /**
     * 最近のログを取得
     */
    fun readRecentLogs(
        maxLines: Int = 200
    ): String {

        val text =
            getLogText()

        if (
            text.isBlank() ||
            text == "ログはありません。"
        ) {
            return text
        }

        val lines =
            text.lines()

        val filteredLines =
            lines.filter {
                it.isNotBlank()
            }

        val safeMaxLines =
            maxLines.coerceAtLeast(1)

        val recentLines =
            filteredLines.takeLast(
                safeMaxLines
            )

        return recentLines.joinToString(
            separator = "\n"
        )
    }

    /**
     * ログファイル取得
     */
    fun getLogFile(): File? {

        val context =
            appContext

        if (context == null) {
            return null
        }

        return File(
            context.filesDir,
            LOG_FILE_NAME
        )
    }

    /**
     * 診断情報
     *
     * 端末のHEVCデコーダー能力も調査する。
     */
    fun getDiagnosticInfo(): String {

        val context =
            appContext

        if (context == null) {

            val builder =
                StringBuilder()

            builder.appendLine(
                "MyHomeCam 診断情報"
            )

            builder.appendLine()

            builder.appendLine(
                "Logger: 未初期化"
            )

            return builder.toString()
        }

        val logFile =
            File(
                context.filesDir,
                LOG_FILE_NAME
            )

        val crashFile =
            File(
                context.filesDir,
                CRASH_LOG_FILE_NAME
            )

        val builder =
            StringBuilder()

        builder.appendLine(
            "MyHomeCam 診断情報"
        )

        builder.appendLine()

        builder.appendLine(
            "Package: ${context.packageName}"
        )

        builder.appendLine(
            "Android: ${Build.VERSION.RELEASE}"
        )

        builder.appendLine(
            "SDK: ${Build.VERSION.SDK_INT}"
        )

        builder.appendLine(
            "Device: ${Build.MANUFACTURER} ${Build.MODEL}"
        )

        builder.appendLine()

        builder.appendLine(
            "Logger: 初期化済み"
        )

        builder.appendLine(
            "Log file: ${logFile.absolutePath}"
        )

        builder.appendLine(
            "Log exists: ${logFile.exists()}"
        )

        builder.appendLine(
            "Log size: ${logFile.length()} bytes"
        )

        builder.appendLine(
            "Crash log: ${crashFile.exists()}"
        )

        builder.appendLine(
            "Crash log size: ${crashFile.length()} bytes"
        )

        builder.appendLine()

        appendHevcDiagnostic(
            builder = builder
        )

        return builder.toString()
    }

    /**
     * HEVCデコーダー診断
     *
     * 現在問題になっている
     * 2304 x 2592 / HEVC
     * を端末のMediaCodecが受け付けられるか確認する。
     */
    private fun appendHevcDiagnostic(
        builder: StringBuilder
    ) {

        builder.appendLine(
            "===== HEVC Decoder Diagnostic ====="
        )

        builder.appendLine(
            "MIME: $HEVC_MIME"
        )

        builder.appendLine(
            "Test resolution: ${TEST_WIDTH}x${TEST_HEIGHT}"
        )

        builder.appendLine(
            "Test frame rate: ${TEST_FRAME_RATE} fps"
        )

        try {

            val codecList =
                MediaCodecList(
                    MediaCodecList.ALL_CODECS
                )

            val codecInfos =
                codecList.codecInfos

            var foundDecoder =
                false

            codecInfos.forEach { codecInfo ->

                if (codecInfo.isEncoder) {
                    return@forEach
                }

                val supportedTypes =
                    codecInfo.supportedTypes

                val supportsHevc =
                    supportedTypes.any {
                        it.equals(
                            HEVC_MIME,
                            ignoreCase = true
                        )
                    }

                if (!supportsHevc) {
                    return@forEach
                }

                foundDecoder = true

                builder.appendLine()

                builder.appendLine(
                    "Decoder: ${codecInfo.name}"
                )

                if (Build.VERSION.SDK_INT >= 29) {

                    builder.appendLine(
                        "hardwareAccelerated: " +
                            codecInfo.isHardwareAccelerated
                    )

                    builder.appendLine(
                        "softwareOnly: " +
                            codecInfo.isSoftwareOnly
                    )

                    builder.appendLine(
                        "vendor: " +
                            codecInfo.isVendor
                    )
                }

                try {

                    val capabilities =
                        codecInfo.getCapabilitiesForType(
                            HEVC_MIME
                        )

                    appendCodecCapabilities(
                        builder = builder,
                        capabilities = capabilities
                    )

                } catch (e: Exception) {

                    builder.appendLine(
                        "Capabilities取得失敗: " +
                            "${e.javaClass.simpleName}: " +
                            "${e.message}"
                    )
                }
            }

            if (!foundDecoder) {

                builder.appendLine()

                builder.appendLine(
                    "HEVCデコーダー: 見つかりません"
                )

                builder.appendLine(
                    "video/hevc をサポートする" +
                        " MediaCodec がありません。"
                )
            }

        } catch (e: Exception) {

            builder.appendLine()

            builder.appendLine(
                "HEVC診断失敗: " +
                    "${e.javaClass.simpleName}: " +
                    "${e.message}"
            )
        }

        builder.appendLine(
            "===== HEVC Decoder Diagnostic End ====="
        )
    }

    /**
     * MediaCodecの能力情報を診断文字列へ追加
     */
    private fun appendCodecCapabilities(
        builder: StringBuilder,
        capabilities: MediaCodecInfo.CodecCapabilities
    ) {

        val videoCapabilities =
            capabilities.videoCapabilities

        if (videoCapabilities == null) {

            builder.appendLine(
                "VideoCapabilities: なし"
            )

        } else {

            val maxWidth =
                videoCapabilities.supportedWidths.upper

            val maxHeight =
                videoCapabilities.supportedHeights.upper

            builder.appendLine(
                "Max width: $maxWidth"
            )

            builder.appendLine(
                "Max height: $maxHeight"
            )
			
val sizeSupported =
    try {

        videoCapabilities
            .areSizeAndRateSupported(
                TEST_WIDTH,
                TEST_HEIGHT,
                1.0
            )

    } catch (e: Exception) {

        false
    }

builder.appendLine(
    "2304x2592 supported: " +
        sizeSupported
)

            val sizeAndRateSupported =
                try {

                    videoCapabilities
                        .areSizeAndRateSupported(
                            TEST_WIDTH,
                            TEST_HEIGHT,
                            TEST_FRAME_RATE
                        )

                } catch (e: Exception) {

                    false
                }

            builder.appendLine(
                "2304x2592@30fps supported: " +
                    sizeAndRateSupported
            )

            val maxFrameRate =
                try {

                    videoCapabilities
                        .getSupportedFrameRatesFor(
                            TEST_WIDTH,
                            TEST_HEIGHT
                        )

                } catch (e: Exception) {

                    null
                }

            if (maxFrameRate != null) {

                builder.appendLine(
                    "2304x2592 frame rate range: " +
                        "${maxFrameRate.lower} - " +
                        "${maxFrameRate.upper}"
                )
            }
        }

        builder.appendLine(
            "HEVC Profile/Level:"
        )

        if (capabilities.profileLevels.isEmpty()) {

            builder.appendLine(
                "  none"
            )

        } else {

            capabilities.profileLevels.forEach { profileLevel ->

                builder.appendLine(
                    "  profile=${profileLevel.profile}" +
                        " level=${profileLevel.level}"
                )
            }
        }
    }

    /**
     * 未捕捉例外ハンドラー登録
     */
    private fun installCrashHandler() {

        val previousHandler =
            Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler {
                thread,
                throwable ->

            try {

                writeCrashLog(
                    thread = thread,
                    throwable = throwable
                )

            } catch (e: Exception) {

                Log.e(
                    TAG,
                    "クラッシュログ保存失敗",
                    e
                )
            }

            try {

                if (previousHandler != null) {

                    previousHandler.uncaughtException(
                        thread,
                        throwable
                    )
                }

            } catch (e: Exception) {

                Log.e(
                    TAG,
                    "以前のCrashHandler実行失敗",
                    e
                )
            }
        }
    }

    /**
     * クラッシュ情報保存
     */
    private fun writeCrashLog(
        thread: Thread,
        throwable: Throwable
    ) {

        val context =
            appContext

        if (context == null) {
            return
        }

        val file =
            File(
                context.filesDir,
                CRASH_LOG_FILE_NAME
            )

        try {

            val timestamp =
                SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss.SSS",
                    Locale.JAPAN
                ).format(
                    Date()
                )

            val builder =
                StringBuilder()

            builder.appendLine()

            builder.appendLine(
                "========================================"
            )

            builder.appendLine(
                "CRASH $timestamp"
            )

            builder.appendLine(
                "Thread: ${thread.name}"
            )

            builder.appendLine(
                "Exception: ${throwable.javaClass.name}"
            )

            builder.appendLine(
                "Message: ${throwable.message}"
            )

            builder.appendLine()

            builder.appendLine(
                throwable.stackTraceToString()
            )

            builder.appendLine(
                "========================================"
            )

            synchronized(lock) {

                file.appendText(
                    text = builder.toString(),
                    charset = Charsets.UTF_8
                )
            }

        } catch (e: Exception) {

            Log.e(
                TAG,
                "クラッシュログ書き込み失敗",
                e
            )
        }
    }

    /**
     * クラッシュログ削除
     */
    fun clearCrashLog() {

        val context =
            appContext

        if (context == null) {
            return
        }

        val file =
            File(
                context.filesDir,
                CRASH_LOG_FILE_NAME
            )

        try {

            synchronized(lock) {

                file.delete()
            }

        } catch (e: Exception) {

            Log.e(
                TAG,
                "ログ削除に失敗",
                e
            )
        }
    }
}