package com.example.mymusicapp;

import android.graphics.Bitmap;
import android.net.Uri;

public class Song {
    private final Uri songUri;
    private final String name;
    private final int duration;
    private final String artist;
    private final Bitmap albumCover;

    public Song(Uri songUri, String name, int duration, String artist, Bitmap albumCover) {
        this.songUri = songUri;
        this.name = name;
        this.duration = duration;
        this.artist = artist;
        this.albumCover = albumCover;
    }

    public Uri getSongUri() {
        return songUri;
    }

    public String getName() {
        return name;
    }

    public int getDuration() {
        return duration;
    }

    public String getArtist() {
        return artist;
    }

    public Bitmap getAlbumCover() {
        return albumCover;
    }

    @Override
    public String toString() {

        return this.name;
    }
}
