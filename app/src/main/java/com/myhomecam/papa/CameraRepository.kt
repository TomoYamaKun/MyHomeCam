//app/src/main/java/com/myhomecam/papa/CameraRepository.kt
//ver 1.01-01

package com.myhomecam.papa

import android.content.Context

class CameraRepository(context: Context) {

    companion object {
        private const val PREF_NAME = "myhomecam_cameras"
        private const val MAX_CAMERAS = 8
    }

    private val preferences =
        context.applicationContext.getSharedPreferences(
            PREF_NAME,
            Context.MODE_PRIVATE
        )

    fun getConfiguredCameras(): List<CameraConfig> {
        return (1..MAX_CAMERAS)
            .mapNotNull { id ->
                loadCamera(id)?.takeIf { it.isConfigured() }
            }
    }

    fun getCamera(id: Int): CameraConfig {
        return loadCamera(id) ?: CameraConfig(id)
    }

    fun saveCamera(camera: CameraConfig) {
        preferences.edit()
            .putString(key(camera.id, "name"), camera.name)
            .putString(key(camera.id, "ip"), camera.ipAddress)
            .putString(key(camera.id, "user"), camera.userName)
            .putString(key(camera.id, "password"), camera.password)
            .putString(key(camera.id, "rtspPath"), camera.rtspPath)
            .apply()
    }

    fun deleteCamera(id: Int) {
        preferences.edit()
            .remove(key(id, "name"))
            .remove(key(id, "ip"))
            .remove(key(id, "user"))
            .remove(key(id, "password"))
            .remove(key(id, "rtspPath"))
            .apply()
    }

    fun clearAll() {
        preferences.edit().clear().apply()
    }

    fun findFirstEmptySlot(): Int? {
        return (1..MAX_CAMERAS).firstOrNull {
            !loadCamera(it).orEmpty().isConfigured()
        }
    }

    private fun loadCamera(id: Int): CameraConfig? {
        val name = preferences.getString(key(id, "name"), null)
        val ip = preferences.getString(key(id, "ip"), null)
        val user = preferences.getString(key(id, "user"), null)
        val password = preferences.getString(key(id, "password"), null)
        val rtspPath = preferences.getString(
            key(id, "rtspPath"),
            "onvif1"
        )

        if (
            name == null &&
            ip == null &&
            user == null &&
            password == null
        ) {
            return null
        }

        return CameraConfig(
            id = id,
            name = name.orEmpty(),
            ipAddress = ip.orEmpty(),
            userName = user.orEmpty(),
            password = password.orEmpty(),
            rtspPath = rtspPath ?: "onvif1"
        )
    }

    private fun key(id: Int, field: String): String {
        return "camera_${id}_$field"
    }

    private fun CameraConfig?.orEmpty(): CameraConfig {
        return this ?: CameraConfig(0)
    }
}