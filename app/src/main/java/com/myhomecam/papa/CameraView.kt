//app/src/main/java/com/myhomecam/papa/CameraView.kt
//ver 1.01-01

package com.myhomecam.papa

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView

class CameraView(
    context: Context,
    private val repository: CameraRepository
) : LinearLayout(context) {

    private val cameraSelectionLayout = LinearLayout(context)
    private val videoAreaTop = TextView(context)
    private val videoAreaBottom = TextView(context)
    private val ptzStatus = TextView(context)

    private var selectedCameraId: Int? = null

    init {
        orientation = VERTICAL
        setPadding(16, 16, 16, 16)

        createSelectionArea()
        createVideoArea()
        createPtzArea()

        refresh()
    }

    private fun createSelectionArea() {
        val title = TextView(context).apply {
            text = "操作対象カメラ"
            textSize = 18f
            setTextColor(Color.WHITE)
            setPadding(0, 0, 0, 8)
        }

        addView(
            title,
            LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
        )

        cameraSelectionLayout.orientation = VERTICAL

        addView(
            cameraSelectionLayout,
            LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
        )
    }

    private fun createVideoArea() {
        videoAreaTop.apply {
            text = "上側映像\n\nRTSP映像待機中"
            textSize = 18f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.rgb(25, 25, 25))
        }

        videoAreaBottom.apply {
            text = "下側映像\n\nRTSP映像待機中"
            textSize = 18f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.rgb(25, 25, 25))
        }

        addView(
            videoAreaTop,
            LayoutParams(
                LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        addView(
            videoAreaBottom,
            LayoutParams(
                LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )
    }

    private fun createPtzArea() {
        ptzStatus.apply {
            text = "PTZ操作対象：未選択"
            textSize = 16f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 12, 0, 12)
        }

        addView(
            ptzStatus,
            LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
        )
    }

    fun refresh() {
        cameraSelectionLayout.removeAllViews()

        val cameras = repository.getConfiguredCameras()

        if (cameras.isEmpty()) {
            val emptyText = TextView(context).apply {
                text = "登録されたカメラはありません。\n設定タブから登録してください。"
                textSize = 16f
                setTextColor(Color.LTGRAY)
                setPadding(0, 8, 0, 16)
            }

            cameraSelectionLayout.addView(emptyText)

            selectedCameraId = null
            ptzStatus.text = "PTZ操作対象：未選択"
            return
        }

        val radioGroup = android.widget.RadioGroup(context).apply {
            orientation = VERTICAL
        }

        cameras.forEach { camera ->
            val radioButton = RadioButton(context).apply {
                text = camera.name
                textSize = 16f
                setTextColor(Color.WHITE)
                tag = camera.id

                isChecked = selectedCameraId == camera.id ||
                        (selectedCameraId == null &&
                                camera.id == cameras.first().id)

                setOnClickListener {
                    selectedCameraId = camera.id
                    ptzStatus.text =
                        "PTZ操作対象：${camera.name}"

                    updateSelection()
                }
            }

            radioGroup.addView(radioButton)

            if (radioButton.isChecked) {
                selectedCameraId = camera.id
                ptzStatus.text =
                    "PTZ操作対象：${camera.name}"
            }
        }

        cameraSelectionLayout.addView(radioGroup)
    }

    private fun updateSelection() {
        val parent = cameraSelectionLayout
            .getChildAt(0)

        if (parent is android.widget.RadioGroup) {
            for (index in 0 until parent.childCount) {
                val child = parent.getChildAt(index)

                if (child is RadioButton) {
                    child.isChecked =
                        child.tag == selectedCameraId
                }
            }
        }
    }
}