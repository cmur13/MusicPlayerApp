package com.example.musicplayer

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.widget.SeekBar
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.musicplayer.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity(), ServiceConnection, MediaPlayer.OnCompletionListener {

    companion object {
        lateinit var musicList: ArrayList<Song>
        var songPosition: Int = 0
        var isPlaying: Boolean = false
        var musicService: MusicService? = null

        @SuppressLint("StaticFieldLeak")
        lateinit var binding: ActivityPlayerBinding

        var repeat: Boolean = false
        var nowPlayingId: String = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        initializeLayout()

        binding.PlayPauseButton.setOnClickListener {
            if (isPlaying) {
                pauseMusic()
            } else {
                playMusic()
            }
        }

        binding.nextButton.setOnClickListener {
            prevNextSong(Increment = true)
        }
        binding.previousPlayer.setOnClickListener {
            prevNextSong(Increment = false)
        }
        binding.backButton.setOnClickListener { finish() }

        binding.timeBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) musicService!!.mediaPlayer!!.seekTo(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })
    }

    private fun setLayout() {
        Glide.with(this)
            .load(musicList[songPosition].artUri)
            .apply(RequestOptions().placeholder(R.mipmap.ic_launcher_round).centerCrop())
            .into(binding.songImageDisplay)

        binding.songName.text = musicList[songPosition].title
    }

    private fun createMediaPlayer() {
        try {
            if (musicService!!.mediaPlayer == null) musicService!!.mediaPlayer = MediaPlayer()
            musicService!!.mediaPlayer!!.reset()
            musicService!!.mediaPlayer!!.setDataSource(musicList[songPosition].path)
            musicService!!.mediaPlayer!!.prepare()
            musicService!!.mediaPlayer!!.start()
            isPlaying = true
            binding.PlayPauseButton.setIconResource(R.drawable.pause_icon)
            musicService!!.showNotification(R.drawable.pause_icon)
            binding.timeStart.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
            binding.timeEnd.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
            binding.timeBar.progress = 0
            binding.timeBar.max = musicService!!.mediaPlayer!!.duration
            musicService!!.mediaPlayer!!.setOnCompletionListener(this)
            nowPlayingId = musicList[songPosition].id
        } catch (e: Exception) {
            return
        }
    }

    private fun initializeLayout() {
        songPosition = intent.getIntExtra("index", 0)
        when (intent.getStringExtra("class")) {
            "MusicAdapter" -> {
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                musicList = ArrayList()
                musicList.addAll(MainActivity.MusicListMA)
                setLayout()
                createMediaPlayer()
            }
            "MainActivity" -> {
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                musicList = ArrayList()
                musicList.addAll(MainActivity.MusicListMA)
                setLayout()
                createMediaPlayer()
            }
            "NowPlaying" -> {
                setLayout()
                binding.timeStart.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
                binding.timeEnd.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
                binding.timeBar.progress = musicService!!.mediaPlayer!!.currentPosition
                binding.timeBar.max = musicService!!.mediaPlayer!!.duration
                if (isPlaying) {
                    binding.PlayPauseButton.setIconResource(R.drawable.pause_icon)
                } else {
                    binding.PlayPauseButton.setIconResource(R.drawable.play_icon)
                }
            }
        }
    }

    private fun playMusic() {
        binding.PlayPauseButton.setIconResource(R.drawable.pause_icon)
        musicService!!.showNotification(R.drawable.pause_icon)
        isPlaying = true
        musicService!!.mediaPlayer!!.start()
    }

    private fun pauseMusic() {
        binding.PlayPauseButton.setIconResource(R.drawable.play_icon)
        musicService!!.showNotification(R.drawable.play_icon)
        isPlaying = false
        musicService!!.mediaPlayer!!.pause()
    }

    private fun prevNextSong(Increment: Boolean) {
        if (Increment) {
            setSongPosition(Increment = true)
            setLayout()
            createMediaPlayer()
        } else {
            setSongPosition(Increment = false)
            setLayout()
            createMediaPlayer()
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MusicService.MyBinder
        musicService = binder.currentService()
        createMediaPlayer()
        musicService!!.seekBarSetup()
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }

    override fun onCompletion(mp: MediaPlayer?) {
        setSongPosition(true)
        createMediaPlayer()
        try {
            setLayout()
        } catch (e: Exception) {
            return
        }
    }
}




