//app/src/main/java/com/myhomecam/papa/SnapshotManager.kt
//ver 1.03-13

package com.myhomecam.papa

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.view.View
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SnapshotManager {

    fun capture(
        context: Context,
        view: View
    ): File? {

        return try {

            if (
                view.width <= 0 ||
                view.height <= 0
            ) {
                AppLogger.info(
                    "SNAPSHOT",
                    "映像サイズが取得できません。"
                )

                return null
            }

            val bitmap =
                Bitmap.createBitmap(
                    view.width,
                    view.height,
                    Bitmap.Config.ARGB_8888
                )

            val canvas =
                android.graphics.Canvas(bitmap)

            view.draw(canvas)

            val directory =
                File(
                    context.getExternalFilesDir(
                        Environment.DIRECTORY_PICTURES
                    ),
                    "snapshots"
                )

            directory.mkdirs()

            val timestamp =
                SimpleDateFormat(
                    "yyyyMMdd_HHmmss_SSS",
                    Locale.JAPAN
                ).format(Date())

            val file =
                File(
                    directory,
                    "snapshot_$timestamp.jpg"
                )

            FileOutputStream(file).use { output ->

                bitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    95,
                    output
                )
            }

            bitmap.recycle()

            AppLogger.info(
                "SNAPSHOT",
                "保存成功 file=${file.absolutePath}"
            )

            file

        } catch (e: Exception) {

            AppLogger.error(
                "SNAPSHOT",
                "スナップショット保存失敗",
                e
            )

            null
        }
    }
}