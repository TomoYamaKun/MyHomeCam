//app/src/main/java/com/myhomecam/papa/CameraConfig.kt
//ver 1.04-01

package com.myhomecam.papa

data class CameraConfig(
    val id: Int,
    var name: String = "",
    var ipAddress: String = "",
    var port: Int = 554,
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

    // 実際のプレイヤー接続用（ポートとパスワードを含む）
    fun rtspUrl(): String {
        return "rtsp://$userName:$password@$ipAddress:$port/$rtspPath"
    }

    // ログ出力や画面表示用（パスワードをマスクしつつポートも含める）
    fun rtspUrlSafe(): String {
        return "rtsp://$userName:****@$ipAddress:$port/$rtspPath"
    }
}
