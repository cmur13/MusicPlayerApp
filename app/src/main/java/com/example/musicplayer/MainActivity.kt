package com.example.musicplayer

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.musicplayer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    // test
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Show toast when buttons are clicked (for now)
        binding.shuffleE.setOnClickListener {
            // For now, show a toast instead of navigating to another activity
            Toast.makeText(this, "Shuffle clicked", Toast.LENGTH_SHORT).show()
        }
        binding.favoriteE.setOnClickListener {
            // For now, show a toast instead of navigating to another activity
            Toast.makeText(this, "Favorite clicked", Toast.LENGTH_SHORT).show()
        }
        binding.playlistE.setOnClickListener {
            // For now, show a toast instead of navigating to another activity
            Toast.makeText(this, "Playlist clicked", Toast.LENGTH_SHORT).show()
        }
    }
    // this is an example
}
