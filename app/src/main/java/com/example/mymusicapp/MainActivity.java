package com.example.mymusicapp;

import static java.lang.String.format;

import android.Manifest;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Size;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST = 1;
    private LinearLayout llCurrentSongInfo;
    private TextView tvCurrentSong, tvCurrentArtist, tvCurrentDuration, tvMaxDuration;
    private ImageView bPlayPause, bPrevious, bNext, bLoop, ivAlbumCover;
    private ListView lvSongs;
    private SeekBar seekBar;
    private MediaPlayer mp;
    private int currentSongPosition;
    private boolean loop = false;

    private List<Song> songList = new ArrayList<Song>();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolBar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolBar);

        llCurrentSongInfo = findViewById(R.id.llCurrentSongInfo);
        tvCurrentSong = findViewById(R.id.tvCurrentSong);
        tvCurrentArtist = findViewById(R.id.tvCurrentArtist);
        tvCurrentDuration = findViewById(R.id.tvCurrentDuration);
        tvMaxDuration = findViewById(R.id.tvMaxDuration);
        bPlayPause = findViewById(R.id.fbPlayPause);
        bPrevious = findViewById(R.id.bPrevious);
        bNext = findViewById(R.id.bNext);
        bLoop = findViewById(R.id.fbLoop);
        ivAlbumCover = findViewById(R.id.ivAlbumCover);
        lvSongs = findViewById(R.id.lvSongs);
        seekBar = findViewById(R.id.seekBar);

        checkPermissions();

        bPlayPause.setOnClickListener(view -> bOnClickPlayPauseSong());

        bLoop.setOnClickListener(view -> {
            bOnClickChangeLoop();
        });

        bNext.setOnClickListener(view -> {
            bOnClickNextSong();
        });

        bPrevious.setOnClickListener(view -> {
            bOnClickPreviousSong();
        });

        lvSongs.setOnItemClickListener((parent, view, position, id) -> {
            bPlaySelectedSong(position);
        });

        if (mp == null) {
            llCurrentSongInfo.setVisibility(View.GONE);
        } else {
            bPlaySelectedSong(lvSongs.getSelectedItemPosition());
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // TODO make song data smaller
        }
    }

    private void bPlaySelectedSong(int position) {
        Song chosenSong = (Song) lvSongs.getItemAtPosition(position);

        resetAndStartMediaPlayer(chosenSong);

        try {
            setSongDataIntoLayout(chosenSong);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        setSongDataIntoSeekbar(chosenSong);

        llCurrentSongInfo.setVisibility(View.VISIBLE);
    }

    private void checkLoop() { // checks if the "loop" button is active or not
        if (loop) {
            mp.release();
            mp.start();
        } else {
            bOnClickNextSong();
        }
    }

    private void bOnClickChangeLoop() {
        if (loop) {
            bLoop.setColorFilter(getColor(R.color.disabled), PorterDuff.Mode.MULTIPLY);
        } else {
            bLoop.setColorFilter(null);
        }
        loop = !loop;
    }

    private void bOnClickPlayPauseSong() {
        if (mp == null) {
            Toast.makeText(this, getString(R.string.toast_no_song), Toast.LENGTH_SHORT).show();
        } else {
            if (!mp.isPlaying()) {
                mp.start();

                bPlayPause.setImageResource(R.drawable.pause_24);
            } else {
                mp.pause();

                bPlayPause.setImageResource(R.drawable.play_24);
            }

            mp.setOnCompletionListener(arg01 -> checkLoop());
        }
    }

    private void bOnClickNextSong() {
        if (currentSongPosition < lvSongs.getCount() - 1) {
            currentSongPosition++;
        } else {
            lvSongs.setSelection(0);

            currentSongPosition = 0;
        }

        bPlaySelectedSong(currentSongPosition);
    }

    private void bOnClickPreviousSong() {
        if (mp.getCurrentPosition() < 3000) {
            if (currentSongPosition > 0) {
                currentSongPosition--;
            } else {
                currentSongPosition = (lvSongs.getCount() - 1);
            }

            bPlaySelectedSong(currentSongPosition);
        } else {
            resetAndStartMediaPlayer((Song) lvSongs.getItemAtPosition(currentSongPosition));
        }
    }

    private void resetAndStartMediaPlayer(Song song) {
        if (mp != null) {
            mp.release();
            mp = null;
        }
        mp = MediaPlayer.create(MainActivity.this, song.getSongUri());
        mp.setOnCompletionListener(mediaPlayer -> checkLoop());
        mp.start();

        bPlayPause.setImageResource(R.drawable.pause_24);
    }

    private void setSongDataIntoLayout(Song song) throws IOException {
        tvCurrentSong.setText(song.getName());
        tvCurrentArtist.setText(song.getArtist());
        ivAlbumCover.setImageBitmap(song.getAlbumCover());
    }
    
    private void setSongDataIntoSeekbar(Song song) { // loads the song duration into the seekbar
        seekBar.setMax(song.getDuration() / 1000);
        
        tvMaxDuration.setText(durationConverter(song.getDuration()));
        tvCurrentDuration.setText(durationConverter(0));
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_AUDIO)) {
                // permission was granted, yay! Do the external storage task you need to do.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_AUDIO}, MY_PERMISSIONS_REQUEST);
                // MY_PERMISSIONS_REQUEST is an app-defined int constant. The callback method gets the result of the request.
            }
        } else {
            updateSongList();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean mediaAudioPermission = false;
        boolean externalStoragePermission = false;

        if (requestCode == MY_PERMISSIONS_REQUEST) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) { // permission was granted, yay! Do the external storage task you need to do.
                updateSongList();
            }
        }
    }

    public void updateSongList() {
        String[] projection = new String[] {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM_ID,
        };
        String selection = MediaStore.Audio.Media.DURATION + " >= ? AND " + MediaStore.Audio.AudioColumns.DATA + " NOT LIKE ?";

        String[] selectionArgs = new String[] {
                String.valueOf(TimeUnit.MILLISECONDS.convert(2, TimeUnit.SECONDS)), "%/WhatsApp/%"
        };
        String sortOrder = MediaStore.Audio.Media.DISPLAY_NAME + " ASC";

        Cursor songDataCursor = getApplicationContext().getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
        );

        int idColumn = songDataCursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
        int nameColumn = songDataCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
        int durationColumn = songDataCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
        int artistColumn = songDataCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
        int albumIdColumn = songDataCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);

        while (songDataCursor.moveToNext()) {
            long songId = songDataCursor.getLong(idColumn);
            String name = songDataCursor.getString(nameColumn);
            int duration = songDataCursor.getInt(durationColumn);
            String artist = songDataCursor.getString(artistColumn);
            long albumId = songDataCursor.getLong(albumIdColumn);

            Uri songContentUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId);

            Bitmap albumThumbnail = null;
            try {
                albumThumbnail = getApplicationContext().getContentResolver().loadThumbnail(ContentUris.withAppendedId(
                        MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, albumId), new Size(100, 100), null);
            } catch (IOException e) {

            }

            songList.add(new Song(songContentUri, name, duration, artist, albumThumbnail));

            ArrayAdapter<Song> arrayAdapter = new ArrayAdapter<Song>( this, android.R.layout.simple_list_item_1, songList);

            lvSongs .setAdapter(arrayAdapter);
        }
        songDataCursor.close();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.actionSearch) {
            // TODO search a string inside the listView and update it to only show songs that match it, else show "No songs match the criteria"
            Toast.makeText(this, getString(R.string.toast_search_todo), Toast.LENGTH_LONG).show();
        }
        if (item.getItemId() == R.id.actionAbout) {
            showDialogAbout();
            return true;
        }
        if (item.getItemId() == R.id.actionExit) {
            showDialogCloseApp();
            return true;
        }
        // If we got here, the user's action was not recognized.
        // Invoke the superclass to handle it.
        return super.onOptionsItemSelected(item);
    }

    private void showDialogAbout() {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_about_title));
        builder.setMessage(getString(R.string.dialog_about_body));

        // add the buttons
        builder.setPositiveButton(getString(R.string.dialog_about_positive), (dialog, which) -> {
            // Do nothing, just close dialog box
        });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showDialogCloseApp() {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.dialog_close_body));

        // add the buttons
        builder.setPositiveButton(getString(R.string.dialog_close_positive), (dialog, which) -> {
            finish();
            System.exit(0);
        });

        builder.setNegativeButton(getString(R.string.dialog_close_negative), (dialog, which) -> {
            // Do nothing, just close dialog box
        });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private String durationConverter(int duration) {
        int minutes = duration / 1000 / 60;
        int remainingSeconds = (duration / 1000) % 60;

        return format(Locale.getDefault(),"%02d:%02d", minutes, remainingSeconds);
    }

    @Override
    public void onBackPressed() {
        showDialogCloseApp();
    }
}