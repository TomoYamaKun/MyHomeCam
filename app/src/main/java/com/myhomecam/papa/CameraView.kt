//app/src/main/java/com/myhomecam/papa/CameraView.kt
//ver 1.02-12

package com.myhomecam.papa

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Button

class CameraView(
    context: Context,
    private val repository: CameraRepository
) : ScrollView(context) {

    private val root =
        LinearLayout(context)

    private val cameraSelectionLayout =
        LinearLayout(context)

    private val videoAreaTop =
        TextView(context)

    private val videoAreaBottom =
        TextView(context)

    private val cameraInfo =
        TextView(context)

    private val ptzStatus =
        TextView(context)

    private var selectedCameraId: Int? = null

    init {

        root.orientation =
            LinearLayout.VERTICAL

        root.setPadding(
            12,
            12,
            12,
            12
        )

        addView(root)

        createSelectionArea()
        createVideoArea()
        createPtzArea()

        refresh()
    }

    private fun createSelectionArea() {

        val title =
            TextView(context).apply {
                text = "操作対象カメラ"
                textSize = 18f
                setTextColor(Color.BLACK)
                setPadding(
                    0,
                    0,
                    0,
                    8
                )
            }

        root.addView(title)

        cameraSelectionLayout.orientation =
            LinearLayout.VERTICAL

        root.addView(
            cameraSelectionLayout,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )
    }

    private fun createVideoArea() {

        videoAreaTop.apply {
            text =
                "上側映像\n\nRTSP映像待機中"

            textSize = 17f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setBackgroundColor(
                Color.rgb(
                    25,
                    25,
                    25
                )
            )
        }

        videoAreaBottom.apply {
            text =
                "下側映像\n\nRTSP映像待機中"

            textSize = 17f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setBackgroundColor(
                Color.rgb(
                    25,
                    25,
                    25
                )
            )
        }

        val height =
            getVideoHeight()

        root.addView(
            videoAreaTop,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(height)
            )
        )

        root.addView(
            videoAreaBottom,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(height)
            )
        )

        cameraInfo.apply {
            text = "カメラ情報：未選択"
            textSize = 14f
            setTextColor(Color.DKGRAY)
            setPadding(
                0,
                8,
                0,
                8
            )
        }

        root.addView(cameraInfo)
    }

    private fun createPtzArea() {

        ptzStatus.apply {
            text = "PTZ操作対象：未選択"
            textSize = 16f
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
            setPadding(
                0,
                8,
                0,
                8
            )
        }

        root.addView(ptzStatus)

        val ptzLayout =
            LinearLayout(context).apply {
                orientation =
                    LinearLayout.VERTICAL

                gravity =
                    Gravity.CENTER
            }

        val upButton =
            createPtzButton("▲") {
                onPtzCommand("UP")
            }

        val middle =
            LinearLayout(context).apply {
                orientation =
                    LinearLayout.HORIZONTAL

                gravity =
                    Gravity.CENTER
            }

        val leftButton =
            createPtzButton("◀") {
                onPtzCommand("LEFT")
            }

        val stopButton =
            createPtzButton("■") {
                onPtzCommand("STOP")
            }

        val rightButton =
            createPtzButton("▶") {
                onPtzCommand("RIGHT")
            }

        val downButton =
            createPtzButton("▼") {
                onPtzCommand("DOWN")
            }

        middle.addView(leftButton)
        middle.addView(stopButton)
        middle.addView(rightButton)

        ptzLayout.addView(upButton)
        ptzLayout.addView(middle)
        ptzLayout.addView(downButton)

        root.addView(ptzLayout)
    }

    private fun createPtzButton(
        text: String,
        action: () -> Unit
    ): Button {

        return Button(context).apply {
            this.text = text
            textSize = 18f

            minWidth = dp(64)
            minHeight = dp(52)

            setOnClickListener {
                action()
            }
        }
    }

    private fun onPtzCommand(
        command: String
    ) {

        val cameraId =
            selectedCameraId

        if (cameraId == null) {

            ptzStatus.text =
                "PTZ操作対象：未選択"

            return
        }

        val camera =
            repository.getCamera(cameraId)

        ptzStatus.text =
            "PTZ：$command / ${camera.name}"

        AppLogger.info(
            "PTZ",
            "UI操作 command=$command cameraId=$cameraId"
        )
    }

    fun refresh() {

        cameraSelectionLayout
            .removeAllViews()

        val cameras =
            repository.getConfiguredCameras()

        if (cameras.isEmpty()) {

            val emptyText =
                TextView(context).apply {
                    text =
                        "登録されたカメラはありません。\n設定タブから登録してください。"
                    textSize = 16f
                    setTextColor(Color.DKGRAY)
                    setPadding(
                        0,
                        8,
                        0,
                        16
                    )
                }

            cameraSelectionLayout
                .addView(emptyText)

            selectedCameraId = null

            updateCameraInfo()

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

        val radioGroup =
            RadioGroup(context).apply {
                orientation =
                    RadioGroup.VERTICAL
            }

        cameras.forEach { camera ->

            val radioButton =
                RadioButton(context).apply {

                    text =
                        "${camera.name}  (${camera.ipAddress})"

                    textSize = 16f
                    setTextColor(Color.BLACK)
                    tag = camera.id

                    isChecked =
                        selectedCameraId ==
                            camera.id

                    setOnClickListener {

                        selectedCameraId =
                            camera.id

                        updateSelection()
                        updateCameraInfo()

                        AppLogger.info(
                            "CAMERA",
                            "選択 cameraId=${camera.id}"
                        )
                    }
                }

            radioGroup.addView(
                radioButton
            )
        }

        cameraSelectionLayout
            .addView(radioGroup)

        updateCameraInfo()
    }

    private fun updateSelection() {

        val parent =
            cameraSelectionLayout
                .getChildAt(0)

        if (parent is RadioGroup) {

            for (
                index in 0 until parent.childCount
            ) {

                val child =
                    parent.getChildAt(index)

                if (child is RadioButton) {

                    child.isChecked =
                        child.tag ==
                            selectedCameraId
                }
            }
        }
    }

    private fun updateCameraInfo() {

        val id =
            selectedCameraId

        if (id == null) {

            cameraInfo.text =
                "カメラ情報：未選択"

            ptzStatus.text =
                "PTZ操作対象：未選択"

            return
        }

        val camera =
            repository.getCamera(id)

        cameraInfo.text =
            buildString {
                append("カメラ：")
                append(camera.name)
                append("\nIP：")
                append(camera.ipAddress)
                append("\nRTSP：")
                append(camera.rtspPath)
            }

        ptzStatus.text =
            "PTZ操作対象：${camera.name}"
    }

    private fun getVideoHeight(): Int {

        return context
            .getSharedPreferences(
                "myhomecam_display",
                Context.MODE_PRIVATE
            )
            .getInt(
                "video_height_dp",
                180
            )
            .coerceIn(
                100,
                320
            )
    }

    private fun dp(value: Int): Int {

        return (
            value *
                resources.displayMetrics.density
            ).toInt()
    }
}