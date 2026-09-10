// /storage/internal_new/project/MyHomeCam/app/src/main/java/com/myhomecam/papa/SettingsView.kt
//ver 1.01-10

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
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast

class SettingsView(
    private val context: Context
) {

    private val repository = CameraRepository(context)

    private lateinit var cameraSpinner: Spinner
    private lateinit var nameEdit: EditText
    private lateinit var ipEdit: EditText
    private lateinit var userEdit: EditText
    private lateinit var passwordEdit: EditText
    private lateinit var rtspSpinner: Spinner

    private var selectedCameraId: Int? = null

    fun createView(): View {
        val scrollView = ScrollView(context)

        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
        }

        val title = TextView(context).apply {
            text = "カメラ設定"
            textSize = 24f
            setTextColor(Color.BLACK)
        }

        root.addView(
            title,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        root.addView(createLabel("編集するカメラ"))

        cameraSpinner = Spinner(context)

        root.addView(
            cameraSpinner,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        root.addView(createLabel("カメラ名"))

        nameEdit = createEditText("例：リビングカメラ")
        root.addView(nameEdit)

        root.addView(createLabel("IPアドレス"))

        ipEdit = createEditText("例：192.168.1.100")
        root.addView(ipEdit)

        root.addView(createLabel("ユーザー名"))

        userEdit = createEditText("カメラのユーザー名")
        root.addView(userEdit)

        root.addView(createLabel("パスワード"))

        passwordEdit = createEditText("カメラのパスワード").apply {
            inputType =
                InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        root.addView(passwordEdit)

        root.addView(createLabel("RTSPパス"))

        rtspSpinner = Spinner(context)

        val rtspItems = listOf(
            "onvif1",
            "stream0"
        )

        rtspSpinner.adapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_dropdown_item,
            rtspItems
        )

        root.addView(
            rtspSpinner,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val saveButton = Button(context).apply {
            text = "保存"
            setOnClickListener {
                saveCamera()
            }
        }

        root.addView(saveButton)

        val deleteButton = Button(context).apply {
            text = "選択中のカメラを削除"
            setOnClickListener {
                deleteSelectedCamera()
            }
        }

        root.addView(deleteButton)

        val resetButton = Button(context).apply {
            text = "全カメラ設定を初期化"
            setOnClickListener {
                resetAllCameras()
            }
        }

        root.addView(resetButton)

        cameraSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectCamera(position)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    selectedCameraId = null
                }
            }

        scrollView.addView(root)

        refresh()

        return scrollView
    }

    /**
     * MainActivityから呼び出して
     * カメラ一覧を最新状態に更新する。
     */
    fun refresh() {
        if (!::cameraSpinner.isInitialized) {
            return
        }

        val items = mutableListOf<String>()

        items.add("＋ 新規登録")

        for (cameraId in 1..CameraRepository.MAX_CAMERAS) {
            val camera = repository.loadCamera(cameraId)

            if (camera.isConfigured()) {
                val displayName =
                    if (camera.name.isBlank()) {
                        "カメラ $cameraId"
                    } else {
                        camera.name
                    }

                items.add("$cameraId : $displayName")
            }
        }

        cameraSpinner.adapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_dropdown_item,
            items
        )

        if (selectedCameraId != null) {
            val position = findSpinnerPosition(selectedCameraId!!)

            if (position >= 0) {
                cameraSpinner.setSelection(position)
                return
            }
        }

        cameraSpinner.setSelection(0)
        clearFields()
    }

    private fun findSpinnerPosition(cameraId: Int): Int {
        var position = 1

        for (id in 1..CameraRepository.MAX_CAMERAS) {
            val camera = repository.loadCamera(id)

            if (camera.isConfigured()) {
                if (id == cameraId) {
                    return position
                }

                position++
            }
        }

        return -1
    }

    private fun selectCamera(position: Int) {
        if (position == 0) {
            selectedCameraId = null
            clearFields()
            return
        }

        val cameraId = getCameraIdFromSpinnerPosition(position)

        if (cameraId == null) {
            selectedCameraId = null
            clearFields()
            return
        }

        selectedCameraId = cameraId

        val camera = repository.loadCamera(cameraId)

        nameEdit.setText(camera.name)
        ipEdit.setText(camera.ipAddress)
        userEdit.setText(camera.username)
        passwordEdit.setText(camera.password)

        val rtspPosition =
            if (camera.rtspPath == "stream0") {
                1
            } else {
                0
            }

        rtspSpinner.setSelection(rtspPosition)
    }

    private fun getCameraIdFromSpinnerPosition(position: Int): Int? {
        if (position <= 0) {
            return null
        }

        var currentPosition = 1

        for (cameraId in 1..CameraRepository.MAX_CAMERAS) {
            val camera = repository.loadCamera(cameraId)

            if (camera.isConfigured()) {
                if (currentPosition == position) {
                    return cameraId
                }

                currentPosition++
            }
        }

        return null
    }

    private fun saveCamera() {
        val cameraId =
            selectedCameraId ?: repository.findFirstEmptySlot()

        if (cameraId == null) {
            Toast.makeText(
                context,
                "登録できるカメラは最大8台です。",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val name = nameEdit.text.toString().trim()
        val ipAddress = ipEdit.text.toString().trim()
        val username = userEdit.text.toString().trim()
        val password = passwordEdit.text.toString()
        val rtspPath = rtspSpinner.selectedItem?.toString() ?: "onvif1"

        if (name.isBlank()) {
            Toast.makeText(
                context,
                "カメラ名を入力してください。",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (ipAddress.isBlank()) {
            Toast.makeText(
                context,
                "IPアドレスを入力してください。",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (username.isBlank()) {
            Toast.makeText(
                context,
                "ユーザー名を入力してください。",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (password.isBlank()) {
            Toast.makeText(
                context,
                "パスワードを入力してください。",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val camera = CameraConfig(
            id = cameraId,
            name = name,
            ipAddress = ipAddress,
            username = username,
            password = password,
            rtspPath = rtspPath
        )

        repository.saveCamera(camera)

        selectedCameraId = cameraId

        Toast.makeText(
            context,
            "カメラ $cameraId を保存しました。",
            Toast.LENGTH_SHORT
        ).show()

        refresh()

        val position = findSpinnerPosition(cameraId)

        if (position >= 0) {
            cameraSpinner.setSelection(position)
        }
    }

    private fun deleteSelectedCamera() {
        val cameraId = selectedCameraId

        if (cameraId == null) {
            Toast.makeText(
                context,
                "削除するカメラを選択してください。",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        repository.deleteCamera(cameraId)

        selectedCameraId = null
        clearFields()
        refresh()

        Toast.makeText(
            context,
            "カメラ $cameraId を削除しました。",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun resetAllCameras() {
        repository.resetAll()

        selectedCameraId = null
        clearFields()
        refresh()

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

    private fun createLabel(text: String): TextView {
        return TextView(context).apply {
            this.text = text
            textSize = 16f
            setTextColor(Color.DKGRAY)
            setPadding(0, 20, 0, 6)
        }
    }

    private fun createEditText(hint: String): EditText {
        return EditText(context).apply {
            this.hint = hint
            textSize = 16f
            setSingleLine(true)
        }
    }
}