package com.example.musicplayer

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayer.databinding.ActivityMainBinding
import java.io.File
import android.Manifest
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var musicPlayer: MusicPlayer // MusicPlayer instance
    //private val songs = mutableListOf<Song>()
    private var favoriteView = false

    // new code
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var musicAdapter: SongAdapter

    companion object {
        private const val REQUEST_CODE = 1
        lateinit var MusicListMA: ArrayList<Song>
        var sortOrder: Int = 0
        val sortingList = arrayOf(
            MediaStore.Audio.Media.DATE_ADDED + " DESC",
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.SIZE + " DESC"
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.playlistE.setOnClickListener {
            val intent = Intent(this, PlaylistActivity::class.java)
            startActivity(intent)
        }

        // Request runtime permissions
        requestRuntimePermission()

        // Initialize RecyclerView
        binding.songsRecyclerView.setHasFixedSize(true)
        binding.songsRecyclerView.layoutManager = LinearLayoutManager(this)

        // Load songs directly
        MusicListMA = getAllAudio()

        // Initialize MusicPlayer instance
        musicPlayer = MusicPlayer(this, MusicListMA)

        // Adapter
        musicAdapter = SongAdapter(this@MainActivity, MusicListMA) { song ->
            musicPlayer.playSong(song)
        }
        binding.songsRecyclerView.adapter = musicAdapter

        // Shuffle button listener
        binding.shuffleE.setOnClickListener {
            if (MusicListMA.isNotEmpty()) {
                val randomIndex = (0 until MusicListMA.size).random() // Generate a random index
                val intent = Intent(this, PlayerActivity::class.java)
                intent.putExtra("index", randomIndex) // Pass the random index
                intent.putExtra("class", "MainActivity")
                startActivity(intent)
            } else {
                Toast.makeText(this, "No songs available to shuffle!", Toast.LENGTH_SHORT).show()
            }
        }

        // Favorites button listener
        binding.favoriteE.setOnClickListener {
            val favoriteSongs = MusicListMA.filter { it.isFavorite }
            if (favoriteView) {
                favoriteView = false
                Toast.makeText(this, "Returning to full list", Toast.LENGTH_SHORT).show()
                musicAdapter.updateMusicList(MusicListMA)
                println("Switching to full list: ${MusicListMA.size} songs")
            } else {
                if (favoriteSongs.isEmpty()) {
                    Toast.makeText(this, "No favorite songs available", Toast.LENGTH_SHORT).show()
                } else {
                    favoriteView = true
                    Toast.makeText(this, "Showing Favorites", Toast.LENGTH_SHORT).show()
                    musicAdapter.updateMusicList(ArrayList(favoriteSongs))
                    println("Switching to favorites: ${favoriteSongs.size} songs")
                }
            }
        }


        // For navigation drawer
        toggle = ActionBarDrawerToggle(this, binding.root, R.string.open, R.string.close)
        binding.root.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }


    // new code
    // For runtime permission request
    private fun requestRuntimePermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 15
            )
        }
    }

    // Function to get all audio files from storage
    @SuppressLint("Range")
    private fun getAllAudio(): ArrayList<Song> {
        val tempList = ArrayList<Song>()
        val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID
        )
        val cursor = this.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null,
            sortingList[sortOrder], null
        )
        if (cursor != null) {
            if (cursor.moveToFirst())
                do {
                    val titleC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)) ?: "Unknown"
                    val idC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID)) ?: "Unknown"
                    val albumC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)) ?: "Unknown"
                    val artistC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)) ?: "Unknown"
                    val pathC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                    val durationC = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                    val albumIdC = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)).toString()
                    val uri = Uri.parse("content://media/external/audio/albumart")
                    val artUriC = Uri.withAppendedPath(uri, albumIdC).toString()
                    val music = Song(
                        id = idC,
                        title = titleC,
                        album = albumC,
                        artist = artistC,
                        path = pathC,
                        duration = durationC,
                        artUri = artUriC
                    )
                    val file = File(music.path)
                    if (file.exists())
                        tempList.add(music)
                } while (cursor.moveToNext())
            cursor.close()
        }
        return tempList
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 15) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_LONG).show()
                MusicListMA = getAllAudio()

                // Adapter
                musicAdapter = SongAdapter(this@MainActivity, MusicListMA){song->
                    musicPlayer.playSong(song)
                }
                binding.songsRecyclerView.adapter = musicAdapter
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 15
                )
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::musicPlayer.isInitialized) {
            musicPlayer.release() // Release resources
        }
        if (!PlayerActivity.isPlaying && PlayerActivity.musicService != null) {
            PlayerActivity.musicService!!.stopForeground(true)
            PlayerActivity.musicService!!.mediaPlayer!!.release()
            PlayerActivity.musicService = null
            exitProcess(1)
        }
    }

    override fun onResume() {
        super.onResume()
        val sortEditor = getSharedPreferences("SORTING", MODE_PRIVATE)
        val sortValue = sortEditor.getInt("sortOrder", 0)
        if (sortOrder != sortValue) {
            sortOrder = sortValue
            MusicListMA = getAllAudio()
            musicAdapter.updateMusicList(MusicListMA)
        }
    }
}

