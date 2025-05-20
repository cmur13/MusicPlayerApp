package com.example.musicplayer


import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.*
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat

class MusicService : Service() {

    private var myBinder = MyBinder()
    var mediaPlayer: MediaPlayer? = null
    private lateinit var mediaSessionCompat: MediaSessionCompat
    private lateinit var runnable: Runnable

    override fun onBind(intent: Intent?): IBinder {
        mediaSessionCompat = MediaSessionCompat(baseContext, "My Music")
        return myBinder
    }

    inner class MyBinder : Binder() {
        fun currentService(): MusicService {
            return this@MusicService
        }
    }

    fun showNotification(playPauseBtn: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val playbackSpeed = if (PlayerActivity.isPlaying) 1F else 0F
            mediaSessionCompat.setMetadata(
                MediaMetadataCompat.Builder()
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mediaPlayer!!.duration.toLong())
                    .build()
            )
            val playbackState = PlaybackStateCompat.Builder()
                .setState(
                    PlaybackStateCompat.STATE_PLAYING,
                    mediaPlayer!!.currentPosition.toLong(),
                    playbackSpeed
                )
                .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                .build()
            mediaSessionCompat.setPlaybackState(playbackState)
        }
    }

    fun createMediaPlayer() {
        try {
            if (PlayerActivity.musicService?.mediaPlayer == null) PlayerActivity.musicService?.mediaPlayer = MediaPlayer()
            PlayerActivity.musicService?.mediaPlayer?.reset()
            PlayerActivity.musicService?.mediaPlayer?.setDataSource(PlayerActivity.musicList[PlayerActivity.songPosition].path)
            PlayerActivity.musicService?.mediaPlayer?.prepare()
            PlayerActivity.binding.PlayPauseButton.setIconResource(R.drawable.pause_icon)
            PlayerActivity.musicService?.showNotification(R.drawable.pause_icon)
            PlayerActivity.binding.timeStart.text = formatDuration(mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.binding.timeEnd.text = formatDuration(mediaPlayer!!.duration.toLong())
            PlayerActivity.binding.timeBar.progress = 0
            PlayerActivity.binding.timeBar.max = mediaPlayer!!.duration
            PlayerActivity.nowPlayingId = PlayerActivity.musicList[PlayerActivity.songPosition].id
        } catch (e: Exception) {
            return
        }
    }

    fun seekBarSetup() {
        runnable = Runnable {
            PlayerActivity.binding.timeStart.text = formatDuration(mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.binding.timeBar.progress = mediaPlayer!!.currentPosition
            Handler(Looper.getMainLooper()).postDelayed(runnable, 200)
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable, 0)
    }
}