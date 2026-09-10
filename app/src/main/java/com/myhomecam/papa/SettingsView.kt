//app/src/main/java/com/myhomecam/papa/SettingsView.kt
//ver 1.02-12

package com.myhomecam.papa

import android.content.Context
import android.graphics.Color
import android.text.InputType
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast

class SettingsView(
    private val context: Context,
    private val repository: CameraRepository,
    private val onChanged: () -> Unit
) {

    private lateinit var cameraSpinner: Spinner
    private lateinit var nameEdit: EditText
    private lateinit var ipEdit: EditText
    private lateinit var userEdit: EditText
    private lateinit var passwordEdit: EditText
    private lateinit var rtspSpinner: Spinner
    private lateinit var videoSizeText: TextView

    private var selectedCameraId: Int? = null

    private val displayPreferences =
        context.getSharedPreferences(
            "myhomecam_display",
            Context.MODE_PRIVATE
        )

    fun createView(): View {

        val scrollView =
            ScrollView(context)

        val root =
            LinearLayout(context).apply {
                orientation =
                    LinearLayout.VERTICAL

                setPadding(
                    24,
                    24,
                    24,
                    24
                )
            }

        val title =
            TextView(context).apply {
                text = "カメラ設定"
                textSize = 24f
                setTextColor(Color.BLACK)
            }

        root.addView(title)

        root.addView(
            createLabel("編集するカメラ")
        )

        cameraSpinner =
            Spinner(context)

        root.addView(cameraSpinner)

        root.addView(
            createLabel("カメラ名")
        )

        nameEdit =
            createEditText(
                "例：リビングカメラ"
            )

        root.addView(nameEdit)

        root.addView(
            createLabel("IPアドレス")
        )

        ipEdit =
            createEditText(
                "例：192.168.1.100"
            )

        root.addView(ipEdit)

        root.addView(
            createLabel("ユーザー名")
        )

        userEdit =
            createEditText(
                "カメラのユーザー名"
            )

        root.addView(userEdit)

        root.addView(
            createLabel("パスワード")
        )

        passwordEdit =
            createEditText(
                "カメラのパスワード"
            ).apply {
                inputType =
                    InputType.TYPE_CLASS_TEXT or
                        InputType.TYPE_TEXT_VARIATION_PASSWORD
            }

        root.addView(passwordEdit)

        root.addView(
            createLabel("RTSPパス")
        )

        rtspSpinner =
            Spinner(context)

        rtspSpinner.adapter =
            ArrayAdapter(
                context,
                android.R.layout.simple_spinner_dropdown_item,
                listOf(
                    "onvif1",
                    "stream0"
                )
            )

        root.addView(rtspSpinner)

        val saveButton =
            Button(context).apply {
                text = "保存"

                setOnClickListener {
                    saveCamera()
                }
            }

        root.addView(saveButton)

        val deleteButton =
            Button(context).apply {
                text = "選択中のカメラを削除"

                setOnClickListener {
                    deleteSelectedCamera()
                }
            }

        root.addView(deleteButton)

        val resetButton =
            Button(context).apply {
                text = "全カメラ設定を初期化"

                setOnClickListener {
                    resetAllCameras()
                }
            }

        root.addView(resetButton)

        root.addView(
            createLabel(
                "カメラ映像表示サイズ"
            )
        )

        videoSizeText =
            TextView(context).apply {
                textSize = 16f
                setTextColor(Color.DKGRAY)
            }

        root.addView(videoSizeText)

        val seekBar =
            SeekBar(context).apply {

                max = 220

                progress =
                    getVideoHeight() - 100

                setOnSeekBarChangeListener(
                    object :
                        SeekBar.OnSeekBarChangeListener {

                        override fun onProgressChanged(
                            seekBar: SeekBar?,
                            progress: Int,
                            fromUser: Boolean
                        ) {

                            val height =
                                progress + 100

                            videoSizeText.text =
                                "1画面：${height}dp"

                            if (fromUser) {

                                displayPreferences
                                    .edit()
                                    .putInt(
                                        "video_height_dp",
                                        height
                                    )
                                    .apply()

                                onChanged()

                                AppLogger.info(
                                    "DISPLAY",
                                    "映像表示サイズ=${height}dp"
                                )
                            }
                        }

                        override fun onStartTrackingTouch(
                            seekBar: SeekBar?
                        ) {
                        }

                        override fun onStopTrackingTouch(
                            seekBar: SeekBar?
                        ) {
                        }
                    }
                )
            }

        root.addView(seekBar)

        val note =
            TextView(context).apply {
                text =
                    "小さくするとPTZ操作が見やすくなります。\n範囲：100～320dp"
                textSize = 13f
                setTextColor(Color.GRAY)
                setPadding(
                    0,
                    4,
                    0,
                    16
                )
            }

        root.addView(note)

        cameraSpinner.onItemSelectedListener =
            object :
                AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectCamera(position)
                }

                override fun onNothingSelected(
                    parent: AdapterView<*>?
                ) {
                    selectedCameraId = null
                }
            }

        scrollView.addView(root)

        updateVideoSizeText()

        refresh()

        return scrollView
    }

    fun refresh() {

        if (!::cameraSpinner.isInitialized) {
            return
        }

        val items =
            mutableListOf<String>()

        items.add("＋ 新規登録")

        for (
            cameraId in
            1..CameraRepository.MAX_CAMERAS
        ) {

            val camera =
                repository.getCamera(cameraId)

            if (camera.isConfigured()) {

                val displayName =
                    if (camera.name.isBlank()) {
                        "カメラ $cameraId"
                    } else {
                        camera.name
                    }

                items.add(
                    "$cameraId : $displayName"
                )
            }
        }

        cameraSpinner.adapter =
            ArrayAdapter(
                context,
                android.R.layout.simple_spinner_dropdown_item,
                items
            )

        if (selectedCameraId != null) {

            val position =
                findSpinnerPosition(
                    selectedCameraId!!
                )

            if (position >= 0) {

                cameraSpinner.setSelection(
                    position
                )

                return
            }
        }

        cameraSpinner.setSelection(0)

        clearFields()
    }

    private fun findSpinnerPosition(
        cameraId: Int
    ): Int {

        var position = 1

        for (
            id in
            1..CameraRepository.MAX_CAMERAS
        ) {

            val camera =
                repository.getCamera(id)

            if (camera.isConfigured()) {

                if (id == cameraId) {
                    return position
                }

                position++
            }
        }

        return -1
    }

    private fun selectCamera(
        position: Int
    ) {

        if (position == 0) {

            selectedCameraId = null

            clearFields()

            return
        }

        val cameraId =
            getCameraIdFromSpinnerPosition(
                position
            )

        if (cameraId == null) {

            selectedCameraId = null

            clearFields()

            return
        }

        selectedCameraId =
            cameraId

        val camera =
            repository.getCamera(cameraId)

        nameEdit.setText(
            camera.name
        )

        ipEdit.setText(
            camera.ipAddress
        )

        userEdit.setText(
            camera.userName
        )

        passwordEdit.setText(
            camera.password
        )

        rtspSpinner.setSelection(
            if (camera.rtspPath == "stream0") {
                1
            } else {
                0
            }
        )
    }

    private fun getCameraIdFromSpinnerPosition(
        position: Int
    ): Int? {

        if (position <= 0) {
            return null
        }

        var currentPosition = 1

        for (
            cameraId in
            1..CameraRepository.MAX_CAMERAS
        ) {

            val camera =
                repository.getCamera(cameraId)

            if (camera.isConfigured()) {

                if (
                    currentPosition ==
                    position
                ) {
                    return cameraId
                }

                currentPosition++
            }
        }

        return null
    }

    private fun saveCamera() {

        val cameraId =
            selectedCameraId
                ?: repository.findFirstEmptySlot()

        if (cameraId == null) {

            Toast.makeText(
                context,
                "登録できるカメラは最大8台です。",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val name =
            nameEdit.text
                .toString()
                .trim()

        val ipAddress =
            ipEdit.text
                .toString()
                .trim()

        val userName =
            userEdit.text
                .toString()
                .trim()

        val password =
            passwordEdit.text
                .toString()

        val rtspPath =
            rtspSpinner.selectedItem
                ?.toString()
                ?: "onvif1"

        if (name.isBlank()) {
            showError("カメラ名を入力してください。")
            return
        }

        if (ipAddress.isBlank()) {
            showError("IPアドレスを入力してください。")
            return
        }

        if (userName.isBlank()) {
            showError("ユーザー名を入力してください。")
            return
        }

        if (password.isBlank()) {
            showError("パスワードを入力してください。")
            return
        }

        val camera =
            CameraConfig(
                id = cameraId,
                name = name,
                ipAddress = ipAddress,
                userName = userName,
                password = password,
                rtspPath = rtspPath
            )

        repository.saveCamera(camera)

        selectedCameraId =
            cameraId

        AppLogger.info(
            "CAMERA",
            "保存 cameraId=$cameraId name=$name ip=$ipAddress rtsp=$rtspPath"
        )

        Toast.makeText(
            context,
            "カメラ $cameraId を保存しました。",
            Toast.LENGTH_SHORT
        ).show()

        refresh()

        val position =
            findSpinnerPosition(cameraId)

        if (position >= 0) {
            cameraSpinner.setSelection(position)
        }

        onChanged()
    }

    private fun deleteSelectedCamera() {

        val cameraId =
            selectedCameraId

        if (cameraId == null) {

            showError(
                "削除するカメラを選択してください。"
            )

            return
        }

        repository.deleteCamera(
            cameraId
        )

        AppLogger.info(
            "CAMERA",
            "削除 cameraId=$cameraId"
        )

        selectedCameraId = null

        clearFields()

        refresh()

        onChanged()

        Toast.makeText(
            context,
            "カメラ $cameraId を削除しました。",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun resetAllCameras() {

        repository.clearAll()

        AppLogger.info(
            "CAMERA",
            "全カメラ設定を初期化しました。"
        )

        selectedCameraId = null

        clearFields()

        refresh()

        onChanged()

        Toast.makeText(
            context,
            "全カメラ設定を初期化しました。",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun clearFields() {

        if (!::nameEdit.isInitialized) {
            return
        }

        nameEdit.setText("")
        ipEdit.setText("")
        userEdit.setText("")
        passwordEdit.setText("")

        if (::rtspSpinner.isInitialized) {
            rtspSpinner.setSelection(0)
        }
    }

    private fun getVideoHeight(): Int {

        return displayPreferences
            .getInt(
                "video_height_dp",
                180
            )
            .coerceIn(
                100,
                320
            )
    }

    private fun updateVideoSizeText() {

        if (!::videoSizeText.isInitialized) {
            return
        }

        videoSizeText.text =
            "1画面：${getVideoHeight()}dp"
    }

    private fun showError(
        message: String
    ) {

        Toast.makeText(
            context,
            message,
            Toast.LENGTH_SHORT
        ).show()

        AppLogger.info(
            "SETTINGS",
            message
        )
    }

    private fun createLabel(
        text: String
    ): TextView {

        return TextView(context).apply {
            this.text = text
            textSize = 16f
            setTextColor(Color.DKGRAY)
            setPadding(
                0,
                20,
                0,
                6
            )
        }
    }

    private fun createEditText(
        hint: String
    ): EditText {

        return EditText(context).apply {
            this.hint = hint
            textSize = 16f
            setSingleLine(true)
        }
    }
}