package com.example.musicplayer

data class Playlist(
    val name: String,
    val songs: MutableList<Song>
)
