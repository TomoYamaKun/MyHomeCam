//app/src/main/java/com/myhomecam/papa/RtspPlayer.kt
//ver 1.03-15

package com.myhomecam.papa

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.rtsp.RtspMediaSource
import androidx.media3.ui.PlayerView

class RtspPlayer(
    private val context: Context,
    private val playerView: PlayerView,
    private val onStateChanged: (String) -> Unit
) {

    private var player: ExoPlayer? = null

    fun play(
        url: String
    ) {

        AppLogger.info(
            "RTSP再生要求: $url"
        )

        stop()

        val exoPlayer =
            ExoPlayer.Builder(
                context
            ).build()

        player =
            exoPlayer

        playerView.player =
            exoPlayer

        val mediaItem =
            MediaItem.fromUri(
                url
            )

        val mediaSource =
            RtspMediaSource.Factory()
                .createMediaSource(
                    mediaItem
                )

        exoPlayer.setMediaSource(
            mediaSource
        )

        exoPlayer.addListener(
            object : Player.Listener {

                override fun onPlaybackStateChanged(
                    playbackState: Int
                ) {

                    val state =
                        when (playbackState) {

                            Player.STATE_IDLE ->
                                "IDLE"

                            Player.STATE_BUFFERING ->
                                "BUFFERING"

                            Player.STATE_READY ->
                                "READY"

                            Player.STATE_ENDED ->
                                "ENDED"

                            else ->
                                "UNKNOWN"
                        }

                    AppLogger.info(
                        "RTSP再生状態: $state"
                    )

                    onStateChanged(
                        state
                    )
                }

                override fun onIsPlayingChanged(
                    isPlaying: Boolean
                ) {

                    val state =
                        if (isPlaying) {
                            "PLAYING"
                        } else {
                            "NOT_PLAYING"
                        }

                    AppLogger.info(
                        "RTSP再生中状態: $state"
                    )

                    onStateChanged(
                        state
                    )
                }

                override fun onPlayerError(
                    error: androidx.media3.common.PlaybackException
                ) {

                    AppLogger.error(
                        "RTSP再生エラー: ${error.message}"
                    )

                    onStateChanged(
                        "ERROR: ${error.message}"
                    )
                }
            }
        )

        exoPlayer.prepare()

        exoPlayer.playWhenReady =
            true

        AppLogger.info(
            "RTSPプレイヤーprepare完了"
        )
    }

    fun stop() {

        val currentPlayer =
            player

        if (currentPlayer != null) {

            AppLogger.info(
                "RTSPプレイヤー停止"
            )

            currentPlayer.stop()

            currentPlayer.release()
        }

        player = null

        playerView.player =
            null

        onStateChanged(
            "STOPPED"
        )
    }

    fun isPlaying(): Boolean {

        return player?.isPlaying == true
    }
}