package com.example.musicplayer

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.musicplayer.databinding.FragmentNowPlayingBinding


class NowPlaying : Fragment() {

    companion object{
        lateinit var binding: FragmentNowPlayingBinding
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_now_playing, container, false)
        binding = FragmentNowPlayingBinding.bind(view)


        binding.root.visibility = View.INVISIBLE
        // Inflate the layout for this fragment

        binding.playPause.setOnClickListener {
            if(PlayerActivity.isPlaying) pauseMusic()
            else playMusic()
        }

        binding.nextPlayer.setOnClickListener {

            setSongPosition(Increment = true)
            PlayerActivity.musicService!!.createMediaPlayer()

            PlayerActivity.binding.songName.text = PlayerActivity.musicList[PlayerActivity.songPosition].title
            Glide.with(this)
                .load(PlayerActivity.musicList[PlayerActivity.songPosition].artUri)
                .apply(RequestOptions().placeholder(R.mipmap.ic_launcher_round).centerCrop())
                .into(binding.songImageBar)

            binding.songNamePlayer.text = PlayerActivity.musicList[PlayerActivity.songPosition].title
            PlayerActivity.musicService!!.showNotification(R.drawable.pause_icon)
            playMusic()

        }

        binding.root.setOnClickListener {
            val intent = Intent(requireContext(),PlayerActivity::class.java)

            intent.putExtra("index",PlayerActivity.songPosition)
            intent.putExtra("class","NowPlaying")

            ContextCompat.startActivity(requireContext(),intent,null)
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        if(PlayerActivity.musicService != null){
            binding.root.visibility = View.VISIBLE

            binding.songNamePlayer.isSelected = true

            Glide.with(this)
                .load(PlayerActivity.musicList[PlayerActivity.songPosition].artUri)
                .apply(RequestOptions().placeholder(R.mipmap.ic_launcher_round).centerCrop())
                .into(binding.songImageBar)

            binding.songNamePlayer.text = PlayerActivity.musicList[PlayerActivity.songPosition].title

            if(PlayerActivity.isPlaying) binding.playPause.setIconResource(R.drawable.pause_icon)
            else binding.playPause.setIconResource(R.drawable.play_icon)


        }

    }

    private fun playMusic(){
        PlayerActivity.isPlaying = true
        PlayerActivity.musicService!!.mediaPlayer!!.start()
        binding.playPause.setIconResource(R.drawable.pause_icon)
        PlayerActivity.musicService!!.showNotification(R.drawable.pause_icon)
    }
    private fun pauseMusic(){
        PlayerActivity.isPlaying = false
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        binding.playPause.setIconResource(R.drawable.play_icon)
        PlayerActivity.musicService!!.showNotification(R.drawable.play_icon)
    }
}