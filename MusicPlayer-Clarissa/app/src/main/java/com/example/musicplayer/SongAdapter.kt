package com.example.musicplayer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import com.example.musicplayer.MainActivity.Companion.MusicListMA

class SongAdapter(
    private val context: Context,
    private var songs: ArrayList<Song>,
    private val onSongClick: (Song) -> Unit // Callback function for click events
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.songTitle)
        val artistTextView: TextView = itemView.findViewById(R.id.songArtist)
        val durationTextView: TextView = itemView.findViewById(R.id.songDuration)
        val favoriteIcon: ImageView = itemView.findViewById(R.id.favoriteIcon) // ImageView for favorite icon
        val moreIcon: ImageView = itemView.findViewById(R.id.moreIcon) //ImageView for more options (add/delete/etc.)
        val root: View = itemView
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.song_item, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.titleTextView.text = song.title
        holder.artistTextView.text = song.artist
        // Convert duration (Long) to a human-readable format
        val minutes = song.duration / 1000 / 60
        val seconds = song.duration / 1000 % 60
        holder.durationTextView.text = String.format("%d:%02d", minutes, seconds)

        // Load the favorite state from SharedPreferences
        val sharedPreferences: SharedPreferences = holder.itemView.context.getSharedPreferences("Favorites", Context.MODE_PRIVATE)
        val isFavorite = sharedPreferences.getBoolean(song.title, false) // Default to false if not found
        song.isFavorite = isFavorite

        // Set the favorite icon based on the song's favorite status
        if (song.isFavorite) {
            holder.favoriteIcon.setImageResource(R.drawable.favourite_icon) // Filled star
        } else {
            holder.favoriteIcon.setImageResource(R.drawable.ic_favorite_border) // Empty star
        }

        holder.itemView.setOnClickListener {
            onSongClick(song)
        }

        holder.favoriteIcon.setOnClickListener {
            song.isFavorite = !song.isFavorite
            saveFavoriteState(holder.itemView.context, song.title, song.isFavorite)

            // Sync the change back to MusicListMA
            val index = MusicListMA.indexOfFirst { it.title == song.title }
            if (index != -1) {
                MusicListMA[index].isFavorite = song.isFavorite
            }

            // Update icon and show a message
            if (song.isFavorite) {
                holder.favoriteIcon.setImageResource(R.drawable.favourite_icon)
                Toast.makeText(context, "Marked as favorite", Toast.LENGTH_SHORT).show()
            } else {
                holder.favoriteIcon.setImageResource(R.drawable.ic_favorite_border)
                Toast.makeText(context, "Removed from favorites", Toast.LENGTH_SHORT).show()
            }
        }


        holder.moreIcon.setOnClickListener{
            val popupMenu = PopupMenu(holder.itemView.context,holder.moreIcon)
            popupMenu.menuInflater.inflate(R.menu.popup, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener{ menuItem ->
                val selected = songs[position]
                val intent = Intent(holder.itemView.context, PlaylistActivity::class.java)
                //intent.putExtra("test", selected)
                holder.itemView.context.startActivity(intent)
                true
            }
            popupMenu.show()
        }
        // Handle click events to open PlayerActivity
        holder.root.setOnClickListener {
            val intent = Intent(context, PlayerActivity::class.java)
            intent.putExtra("index", position)
            intent.putExtra("class", "MusicAdapter")
            ContextCompat.startActivity(context, intent, null)
        }
    }


    private fun saveFavoriteState(context: Context, songId: String, isFavorite: Boolean) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("Favorites", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(songId, isFavorite)
        editor.apply() // Apply changes to persist the state
    }

    override fun getItemCount(): Int {
        return songs.size
    }
    fun updateMusicList(newList: ArrayList<Song>) {
        songs = newList
        notifyDataSetChanged()
    }
}





