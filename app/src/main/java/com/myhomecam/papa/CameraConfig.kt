//app/src/main/java/com/myhomecam/papa/CameraConfig.kt
//ver 1.01-01

package com.myhomecam.papa

data class CameraConfig(
    val id: Int,
    var name: String = "",
    var ipAddress: String = "",
    var userName: String = "",
    var password: String = "",
    var rtspPath: String = "onvif1"
) {
    fun isConfigured(): Boolean {
        return name.isNotBlank() &&
                ipAddress.isNotBlank() &&
                userName.isNotBlank() &&
                password.isNotBlank()
    }

    fun rtspUrl(): String {
        return "rtsp://$userName:$password@$ipAddress:554/$rtspPath"
    }
}