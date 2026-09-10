//app/src/main/java/com/myhomecam/papa/CameraView.kt
//ver 1.03-15

package com.myhomecam.papa

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.media3.ui.PlayerView

class CameraView(
    context: Context,
    private val repository: CameraRepository
) : ScrollView(context) {

    private val contentLayout = LinearLayout(context)

    private val cameraSelectionLayout = LinearLayout(context)

    private val videoAreaTop = PlayerView(context)

    private val videoAreaBottom = PlayerView(context)

    private val cameraInfo = TextView(context)

    private val videoStatus = TextView(context)

    private val startStopButton = Button(context)

    private val snapshotButton = Button(context)

    private val recordButton = Button(context)

    private val ptzStatus = TextView(context)

    private var selectedCameraId: Int? = null

    private var isVideoPlaying = false

    private var topPlayer: RtspPlayer? = null

    private var bottomPlayer: RtspPlayer? = null

    init {
        setFillViewport(true)

        setBackgroundColor(Color.BLACK)

        contentLayout.orientation = LinearLayout.VERTICAL

        contentLayout.setPadding(
            dpToPx(12),
            dpToPx(12),
            dpToPx(12),
            dpToPx(20)
        )

        addView(
            contentLayout,
            LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
        )

        createSelectionArea()

        createVideoArea()

        createVideoControlArea()

        createPtzArea()

        refresh()

        AppLogger.info("CameraView 初期化完了")
    }

    private fun createSelectionArea() {

        val title = TextView(context).apply {

            text = "操作対象カメラ"

            textSize = 18f

            setTextColor(Color.WHITE)

            setPadding(
                0,
                0,
                0,
                dpToPx(6)
            )
        }

        contentLayout.addView(
            title,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        cameraSelectionLayout.orientation =
            LinearLayout.HORIZONTAL

        cameraSelectionLayout.gravity =
            Gravity.CENTER_VERTICAL

        contentLayout.addView(
            cameraSelectionLayout,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        cameraInfo.apply {

            text = "カメラ：未選択"

            textSize = 14f

            setTextColor(Color.LTGRAY)

            setPadding(
                0,
                dpToPx(6),
                0,
                dpToPx(4)
            )
        }

        contentLayout.addView(
            cameraInfo,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )
    }

    private fun createVideoArea() {

        val videoHeight = getVideoHeight()

        videoAreaTop.apply {

            setBackgroundColor(Color.BLACK)

            useController = false

            resizeMode =
                androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT

            contentDescription = "上側RTSP映像"
        }

        videoAreaBottom.apply {

            setBackgroundColor(Color.BLACK)

            useController = false

            resizeMode =
                androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT

            contentDescription = "下側RTSP映像"
        }

        contentLayout.addView(
            createVideoLabel("上側映像"),
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        contentLayout.addView(
            videoAreaTop,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                videoHeight
            )
        )

        contentLayout.addView(
            createVideoLabel("下側映像"),
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        contentLayout.addView(
            videoAreaBottom,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                videoHeight
            )
        )

        videoStatus.apply {

            text = "動画：停止中"

            textSize = 14f

            setTextColor(Color.LTGRAY)

            gravity = Gravity.CENTER

            setPadding(
                0,
                dpToPx(6),
                0,
                dpToPx(6)
            )
        }

        contentLayout.addView(
            videoStatus,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )
    }

    private fun createVideoLabel(
        label: String
    ): TextView {

        return TextView(context).apply {

            text = label

            textSize = 14f

            setTextColor(Color.LTGRAY)

            setPadding(
                0,
                dpToPx(4),
                0,
                dpToPx(2)
            )
        }
    }

    private fun createVideoControlArea() {

        startStopButton.apply {

            text = "▶ 動画表示スタート"

            textSize = 16f

            setOnClickListener {

                toggleVideo()
            }
        }

        contentLayout.addView(
            startStopButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        snapshotButton.apply {

            text = "📷 スナップショット"

            textSize = 16f

            setOnClickListener {

                takeSnapshot()
            }
        }

        contentLayout.addView(
            snapshotButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        recordButton.apply {

            text = "● 録画"

            textSize = 16f

            setOnClickListener {

                startRecording()
            }
        }

        contentLayout.addView(
            recordButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )
    }

    private fun createPtzArea() {

        val title = TextView(context).apply {

            text = "PTZ操作"

            textSize = 18f

            setTextColor(Color.WHITE)

            gravity = Gravity.CENTER

            setPadding(
                0,
                dpToPx(10),
                0,
                dpToPx(6)
            )
        }

        contentLayout.addView(
            title,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val rowUp = LinearLayout(context).apply {

            orientation = LinearLayout.HORIZONTAL

            gravity = Gravity.CENTER
        }

        rowUp.addView(
            createPtzButton("▲") {
                sendPtzCommand("上")
            }
        )

        contentLayout.addView(
            rowUp,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val rowMiddle = LinearLayout(context).apply {

            orientation = LinearLayout.HORIZONTAL

            gravity = Gravity.CENTER
        }

        rowMiddle.addView(
            createPtzButton("◀") {
                sendPtzCommand("左")
            }
        )

        rowMiddle.addView(
            createPtzButton("■") {
                sendPtzCommand("停止")
            }
        )

        rowMiddle.addView(
            createPtzButton("▶") {
                sendPtzCommand("右")
            }
        )

        contentLayout.addView(
            rowMiddle,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val rowDown = LinearLayout(context).apply {

            orientation = LinearLayout.HORIZONTAL

            gravity = Gravity.CENTER
        }

        rowDown.addView(
            createPtzButton("▼") {
                sendPtzCommand("下")
            }
        )

        contentLayout.addView(
            rowDown,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        ptzStatus.apply {

            text = "PTZ操作対象：未選択"

            textSize = 14f

            setTextColor(Color.LTGRAY)

            gravity = Gravity.CENTER

            setPadding(
                0,
                dpToPx(6),
                0,
                dpToPx(10)
            )
        }

        contentLayout.addView(
            ptzStatus,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )
    }

    private fun createPtzButton(
        textValue: String,
        action: () -> Unit
    ): Button {

        return Button(context).apply {

            text = textValue

            textSize = 18f

            setOnClickListener {

                action()
            }

            layoutParams =
                LinearLayout.LayoutParams(
                    dpToPx(68),
                    dpToPx(50)
                ).apply {

                    setMargins(
                        dpToPx(3),
                        dpToPx(2),
                        dpToPx(3),
                        dpToPx(2)
                    )
                }
        }
    }

    fun refresh() {

        AppLogger.info("CameraView refresh 開始")

        cameraSelectionLayout.removeAllViews()

        val cameras =
            repository.getConfiguredCameras()

        if (cameras.isEmpty()) {

            selectedCameraId = null

            cameraInfo.text =
                "カメラ：未登録"

            ptzStatus.text =
                "PTZ操作対象：未選択"

            val emptyText =
                TextView(context).apply {

                    text =
                        "登録されたカメラはありません。\n" +
                        "設定タブから登録してください。"

                    textSize = 16f

                    setTextColor(Color.LTGRAY)

                    setPadding(
                        0,
                        dpToPx(8),
                        0,
                        dpToPx(12)
                    )
                }

            cameraSelectionLayout.addView(
                emptyText,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )

            AppLogger.info(
                "登録カメラなし"
            )

            return
        }

        if (
            selectedCameraId == null ||
            cameras.none {
                it.id == selectedCameraId
            }
        ) {

            selectedCameraId =
                cameras.first().id
        }

        cameras.forEach { camera ->

            val button =
                Button(context).apply {

                    text = camera.id.toString()

                    textSize = 16f

                    tag = camera.id

                    contentDescription =
                        "カメラ${camera.id}"

                    setOnClickListener {

                        selectedCameraId =
                            camera.id

                        updateCameraSelection(
                            camera
                        )
                    }
                }

            cameraSelectionLayout.addView(
                button,
                LinearLayout.LayoutParams(
                    dpToPx(52),
                    dpToPx(48)
                ).apply {

                    setMargins(
                        dpToPx(2),
                        0,
                        dpToPx(2),
                        0
                    )
                }
            )
        }

        val selectedCamera =
            cameras.firstOrNull {
                it.id == selectedCameraId
            } ?: cameras.first()

        selectedCameraId =
            selectedCamera.id

        updateCameraSelection(
            selectedCamera
        )

        AppLogger.info(
            "CameraView refresh 完了: cameras=${cameras.size}"
        )
    }

    private fun updateCameraSelection(
        camera: CameraConfig
    ) {

        cameraInfo.text =
            "カメラ ${camera.id}：${camera.name}\n" +
            "IP：${camera.ipAddress}\n" +
            "RTSP：${camera.rtspPath}"

        ptzStatus.text =
            "PTZ操作対象：${camera.name}"

        for (
            index in 0 until
            cameraSelectionLayout.childCount
        ) {

            val child =
                cameraSelectionLayout.getChildAt(
                    index
                )

            if (child is Button) {

                val id =
                    child.tag as? Int

                child.isSelected =
                    id == selectedCameraId
            }
        }

        AppLogger.info(
            "操作対象カメラ変更: id=${camera.id}, name=${camera.name}"
        )
    }

    private fun toggleVideo() {

        val cameraId =
            selectedCameraId

        if (cameraId == null) {

            Toast.makeText(
                context,
                "カメラを選択してください。",
                Toast.LENGTH_SHORT
            ).show()

            AppLogger.info(
                "動画開始要求: カメラ未選択"
            )

            return
        }

        if (isVideoPlaying) {

            stopVideo()

            return
        }

        val camera =
            repository.getCamera(
                cameraId
            )

        if (!camera.isConfigured()) {

            Toast.makeText(
                context,
                "カメラ設定が不完全です。",
                Toast.LENGTH_SHORT
            ).show()

            AppLogger.info(
                "動画開始失敗: camera=$cameraId 設定不完全"
            )

            return
        }

        startVideo(camera)
    }

    private fun startVideo(
        camera: CameraConfig
    ) {

        AppLogger.info(
            "動画表示開始要求: camera=${camera.id}, ip=${camera.ipAddress}, path=${camera.rtspPath}"
        )

        stopPlayers()

        try {

            topPlayer =
                RtspPlayer(
                    context = context,
                    playerView = videoAreaTop
                ) { state ->

                    AppLogger.info(
                        "上側RTSP状態: $state"
                    )
                }

            bottomPlayer =
                RtspPlayer(
                    context = context,
                    playerView = videoAreaBottom
                ) { state ->

                    AppLogger.info(
                        "下側RTSP状態: $state"
                    )
                }

            topPlayer?.play(
                camera.rtspUrl()
            )

            bottomPlayer?.play(
                camera.rtspUrl()
            )

            isVideoPlaying = true

            startStopButton.text =
                "■ 動画表示ストップ"

            videoStatus.text =
                "動画：再生開始要求済み"

            AppLogger.info(
                "動画表示開始完了: camera=${camera.id}"
            )

        } catch (e: Exception) {

            isVideoPlaying = false

            startStopButton.text =
                "▶ 動画表示スタート"

            videoStatus.text =
                "動画：開始失敗"

            AppLogger.error(
                "動画表示開始失敗: ${e.message}"
            )

            Toast.makeText(
                context,
                "動画表示開始に失敗しました。",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun stopVideo() {

        AppLogger.info(
            "動画表示停止要求"
        )

        stopPlayers()

        isVideoPlaying = false

        startStopButton.text =
            "▶ 動画表示スタート"

        videoStatus.text =
            "動画：停止中"

        AppLogger.info(
            "動画表示停止完了"
        )

        Toast.makeText(
            context,
            "動画表示を停止しました。",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun stopPlayers() {

        try {

            topPlayer?.stop()

        } catch (e: Exception) {

            AppLogger.error(
                "上側プレイヤー停止エラー: ${e.message}"
            )
        }

        try {

            bottomPlayer?.stop()

        } catch (e: Exception) {

            AppLogger.error(
                "下側プレイヤー停止エラー: ${e.message}"
            )
        }

        topPlayer = null

        bottomPlayer = null
    }

    private fun takeSnapshot() {

        if (!isVideoPlaying) {

            Toast.makeText(
                context,
                "先に動画表示を開始してください。",
                Toast.LENGTH_SHORT
            ).show()

            AppLogger.info(
                "スナップショット失敗: 動画停止中"
            )

            return
        }

        try {

            SnapshotManager.capture(
                context,
                videoAreaTop
            )

            AppLogger.info(
                "上側映像スナップショット実行"
            )

            Toast.makeText(
                context,
                "スナップショットを保存しました。",
                Toast.LENGTH_SHORT
            ).show()

        } catch (e: Exception) {

            AppLogger.error(
                "スナップショット失敗: ${e.message}"
            )

            Toast.makeText(
                context,
                "スナップショットに失敗しました。",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun startRecording() {

        AppLogger.info(
            "録画ボタン押下"
        )

        Toast.makeText(
            context,
            "録画機能は次段階で実装します。",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun sendPtzCommand(
        direction: String
    ) {

        val cameraId =
            selectedCameraId

        if (cameraId == null) {

            Toast.makeText(
                context,
                "カメラを選択してください。",
                Toast.LENGTH_SHORT
            ).show()

            AppLogger.info(
                "PTZ操作失敗: カメラ未選択 direction=$direction"
            )

            return
        }

        val camera =
            repository.getCamera(
                cameraId
            )

        ptzStatus.text =
            "PTZ操作：${camera.name} / $direction"

        AppLogger.info(
            "PTZ操作要求: camera=$cameraId direction=$direction"
        )

        Toast.makeText(
            context,
            "PTZ：$direction",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun getVideoHeight(): Int {

        val preferences =
            context.getSharedPreferences(
                "myhomecam_display",
                Context.MODE_PRIVATE
            )

        val heightDp =
            preferences.getInt(
                "video_height_dp",
                150
            ).coerceIn(
                100,
                260
            )

        return dpToPx(
            heightDp
        )
    }

    fun release() {

        AppLogger.info(
            "CameraView release"
        )

        stopPlayers()

        isVideoPlaying = false

        startStopButton.text =
            "▶ 動画表示スタート"

        videoStatus.text =
            "動画：停止中"
    }

    private fun dpToPx(
        dp: Int
    ): Int {

        return (
            dp *
                resources.displayMetrics.density
            ).toInt()
    }

    override fun onDetachedFromWindow() {

        release()

        super.onDetachedFromWindow()
    }
}