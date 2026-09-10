//app/src/main/java/com/myhomecam/papa/LogView.kt
//ver 1.02-12

package com.myhomecam.papa

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class LogView(
    private val context: Context,
    private val repository: CameraRepository
) {

    private lateinit var logTextView: TextView
    private lateinit var diagnosticTextView: TextView

    fun createView(): View {

        val scrollView =
            ScrollView(context)

        val root =
            LinearLayout(context).apply {
                orientation =
                    LinearLayout.VERTICAL

                setPadding(
                    16,
                    16,
                    16,
                    16
                )
            }

        val title =
            TextView(context).apply {
                text = "MyHomeCam ログ・診断"
                textSize = 22f
                setTextColor(Color.BLACK)
                setPadding(
                    0,
                    0,
                    0,
                    12
                )
            }

        root.addView(title)

        val infoTitle =
            TextView(context).apply {
                text = "解析用診断情報"
                textSize = 18f
                setTextColor(Color.BLACK)
            }

        root.addView(infoTitle)

        diagnosticTextView =
            TextView(context).apply {
                textSize = 14f
                setTextColor(Color.DKGRAY)
                setPadding(
                    0,
                    8,
                    0,
                    12
                )
                setTextIsSelectable(true)
            }

        diagnosticTextView.setOnClickListener {
            copyToClipboard(
                diagnosticTextView.text.toString()
            )
        }

        root.addView(
            diagnosticTextView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val copyInfoButton =
            Button(context).apply {
                text = "診断情報をコピー"

                setOnClickListener {
                    copyToClipboard(
                        diagnosticTextView.text.toString()
                    )
                }
            }

        root.addView(copyInfoButton)

        val refreshButton =
            Button(context).apply {
                text = "ログ更新"

                setOnClickListener {
                    refresh()
                }
            }

        root.addView(refreshButton)

        val clearButton =
            Button(context).apply {
                text = "ログ初期化"

                setOnClickListener {
                    AppLogger.clearLogs()
                    refresh()

                    Toast.makeText(
                        context,
                        "ログを初期化しました。",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        root.addView(clearButton)

        val logTitle =
            TextView(context).apply {
                text = "アプリログ\n※ログをタップするとコピー"
                textSize = 18f
                setTextColor(Color.BLACK)
                setPadding(
                    0,
                    16,
                    0,
                    8
                )
            }

        root.addView(logTitle)

        logTextView =
            TextView(context).apply {
                textSize = 13f
                setTextColor(Color.DKGRAY)
                gravity = Gravity.START
                setPadding(
                    8,
                    8,
                    8,
                    8
                )
                setTextIsSelectable(true)
            }

        logTextView.setOnClickListener {
            copyToClipboard(
                logTextView.text.toString()
            )
        }

        root.addView(
            logTextView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        scrollView.addView(root)

        refresh()

        return scrollView
    }

    fun refresh() {

        if (!::logTextView.isInitialized) {
            return
        }

        diagnosticTextView.text =
            buildDiagnosticText()

        val appLog =
            AppLogger.getLogText()

        val crashLog =
            AppLogger.getCrashLogText()

        logTextView.text =
            buildString {

                append(appLog)

                if (
                    crashLog.isNotBlank() &&
                    crashLog != "クラッシュログはありません。"
                ) {
                    append("\n\n")
                    append(
                        "========== CRASH LOG ==========\n"
                    )
                    append(crashLog)
                }
            }
    }

    private fun buildDiagnosticText(): String {

        val cameras =
            repository.getConfiguredCameras()

        val selectedNames =
            cameras.joinToString("\n") { camera ->
                "  [${
                    camera.id
                }] ${
                    camera.name
                } / ${
                    camera.ipAddress
                } / RTSP=${camera.rtspPath}"
            }

        val displayHeight =
            context.getSharedPreferences(
                "myhomecam_display",
                Context.MODE_PRIVATE
            ).getInt(
                "video_height_dp",
                180
            )

        return buildString {

            append("===== MyHomeCam Diagnostic =====\n")
            append("package=com.myhomecam.papa\n")
            append("Android=${Build.VERSION.RELEASE}\n")
            append("SDK=${Build.VERSION.SDK_INT}\n")
            append("device=${Build.MANUFACTURER} ${Build.MODEL}\n")
            append("cameraCount=${cameras.size}\n")
            append("videoHeightDp=$displayHeight\n")
            append("logFile=")
            append(
                AppLogger.getLogFilePath()
            )
            append("\n")

            append("\n[Registered Cameras]\n")

            if (cameras.isEmpty()) {
                append("  none\n")
            } else {
                append(selectedNames)
                append("\n")
            }

            append("\n[Security]\n")
            append(
                "password=NOT_INCLUDED\n"
            )

            append("\n[Current Stage]\n")
            append(
                "RTSP player=not implemented\n"
            )
            append(
                "ONVIF PTZ=UI only\n"
            )
            append(
                "remote access=not implemented\n"
            )
        }
    }

    private fun copyToClipboard(
        text: String
    ) {

        val clipboard =
            context.getSystemService(
                Context.CLIPBOARD_SERVICE
            ) as ClipboardManager

        clipboard.setPrimaryClip(
            ClipData.newPlainText(
                "MyHomeCam Log",
                text
            )
        )

        Toast.makeText(
            context,
            "クリップボードへコピーしました。",
            Toast.LENGTH_SHORT
        ).show()
    }
}