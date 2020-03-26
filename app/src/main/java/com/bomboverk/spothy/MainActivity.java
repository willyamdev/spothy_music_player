package com.bomboverk.spothy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bomboverk.spothy.Datadase.DatabaseHelper;
import com.bomboverk.spothy.MusicAdapter.Music;
import com.bomboverk.spothy.MusicAdapter.MusicAdapter;
import com.bomboverk.spothy.MusicAdapter.ViewHolderMusic;
import com.bomboverk.spothy.Notification.NotificationHelper;
import com.bomboverk.spothy.Notification.NotificationService;
import com.bomboverk.spothy.PlaylistsAdapter.Playlist;
import com.bomboverk.spothy.PlaylistsAdapter.PlaylistManager;
import com.bomboverk.spothy.SpinnerAdapter.PlayListsSpinner;
import com.bomboverk.spothy.SpinnerAdapter.SpinnerPlaylistAdapter;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private static MainActivity ins;
    private Toolbar mToolBar;
    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mToggle;
    private NavigationView navigationView;

    private ArrayList<Music> musics = new ArrayList<Music>();
    private ArrayList<Music> actualPlaylist = new ArrayList<Music>();
    private MusicAdapter adapter;
    private RecyclerView mRecycleViewMusics;

    PlayerBar playerBarManager;
    FrameLayout playerBarView;
    FrameLayout playerBarExpanded;
    FrameLayout playlistView;

    private MediaPlayerManager mediaPlayerManager;

    private MenuItem searchmainicon;
    private MenuItem deletemainicon;

    Animation fadeIn;
    Animation fadeOut;

    Playlist playlistSelectedAdd;
    PlaylistManager playlistManager;
    public Playlist playlistFav;

    DatabaseHelper databaseHelperManager;

    private NotificationService notificationService;
    private ServiceConnection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolBar = findViewById(R.id.act_main_toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("Spothy");
        ins = this;

        mRecycleViewMusics = findViewById(R.id.act_main_recycle);

        playerBarView = findViewById(R.id.act_main_playerbar);
        playerBarExpanded = findViewById(R.id.act_main_pbexpand);
        playlistView = findViewById(R.id.act_main_playlist);

        mDrawer = findViewById(R.id.act_main_drawer);
        navigationView = findViewById(R.id.act_main_navigationView);
        navigationView.bringToFront();
        View header = navigationView.getHeaderView(0);

        mToggle = new ActionBarDrawerToggle(this, mDrawer, R.string.open, R.string.close);
        mDrawer.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        verifyPermission();

        databaseHelperManager = new DatabaseHelper(getApplicationContext());
        databaseHelperManager.verifyPlaylistFav();
        playlistFav = databaseHelperManager.getPlaylistFav();

        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                NotificationService.MyBinder binderr=(NotificationService.MyBinder)service;
                notificationService=binderr.getServiceSystem();
            }
            @Override
            public void onServiceDisconnected(ComponentName name) { }
        };

        bindService(new Intent(this, NotificationService.class), connection, Context.BIND_AUTO_CREATE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent intentservice= new Intent(this, NotificationService.class);
            startService(intentservice);
            //startForegroundService(new Intent(this, NotificationService.class));
        }

        playerBarManager = new PlayerBar(playerBarView, playerBarExpanded, getApplicationContext(), musics, header, this);
        mediaPlayerManager = new MediaPlayerManager(getApplicationContext(), playerBarManager, this);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menu_item_music:
                        playerBarExpanded.setVisibility(View.INVISIBLE);
                        playlistView.setVisibility(View.INVISIBLE);
                        break;
                    case R.id.menu_item_favs:
                        playerBarExpanded.setVisibility(View.INVISIBLE);
                        playlistFav = databaseHelperManager.getPlaylistFav();
                        playlistManager.prepareRecycle(playlistFav, musics);
                        break;
                    case R.id.menu_item_playlist:
                        startActivityForResult(new Intent(MainActivity.this, PlaylistsMusics.class), 1);
                        break;
                }
                mDrawer.closeDrawers();
                return true;
            }
        });

        fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(500);

        fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setDuration(400);

        playlistManager = new PlaylistManager(getApplicationContext(), playlistView, this);
    }

    public NotificationService getNotificationService() {
        return notificationService;
    }

    public static MainActivity getInstace() {
        return ins;
    }

    private void verifyPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            loadMusics();
        }
    }

    private void loadMusics() {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
        String orderBy = MediaStore.Audio.Media.DISPLAY_NAME + " ASC";
        Cursor cursor = getContentResolver().query(uri, null, selection, null, orderBy);

        musics.clear();

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String nome = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                    String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                    Uri url = Uri.withAppendedPath(uri,""+ id);

                    nome = nome.replaceAll(".mp3", "");

                    if (artist.equals("<unknown>")) {
                        artist = getString(R.string.act_main_desconhecido);
                    }

                    Music s = new Music(nome, artist, url.toString(), id);
                    musics.add(s);
                } while (cursor.moveToNext());
            }
            cursor.close();
            actualPlaylist.clear();
            actualPlaylist.addAll(musics);
        }
        loadRecycle();
    }

    private void loadRecycle() {
        adapter = new MusicAdapter(musics, this, false, new MusicAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Music music, ViewHolderMusic vholder, int position) {

                if (adapter.selectedItems.size() > 0) {

                    if (adapter.selectedItems.get(position, false)) {
                        adapter.selectedItems.delete(position);
                        vholder.constraintLayout.setSelected(false);
                    } else {
                        adapter.selectedItems.put(position, true);
                        vholder.constraintLayout.setSelected(true);
                    }

                    if (adapter.selectedItems.size() == 0) {
                        prepareToSelect();
                    }

                } else {
                    actualPlaylist.clear();
                    actualPlaylist.addAll(musics);
                    mediaPlayerManager.startMusic(music);
                }
            }

            @Override
            public void OnLongPress(Music music, ViewHolderMusic vholder, int position) {

                if (!adapter.selectedItems.get(position, false)) {
                    if (adapter.selectedItems.size() == 0) {
                        prepareToSelect();
                    }

                    adapter.selectedItems.put(position, true);
                    vholder.constraintLayout.setSelected(true);
                }
            }
        });

        mRecycleViewMusics.setAdapter(adapter);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecycleViewMusics.setLayoutManager(layoutManager);
    }

    public void startSpecificMusic(Music music, ArrayList<Music> playlistSelect) {
        actualPlaylist.clear();
        actualPlaylist.addAll(playlistSelect);
        mediaPlayerManager.startMusic(music);
    }

    public ArrayList<Music> getActualPlaylist() {
        return actualPlaylist;
    }

    public void startLastMusic(View view) {
        if (mediaPlayerManager.isPlayng()) {
            mediaPlayerManager.pauseMusic();
        } else {
            mediaPlayerManager.resumeMusic(playerBarManager.getLastMusic());
        }
    }

    public void nextMusic(View view) {
        int indexMusic = actualPlaylist.indexOf(playerBarManager.getLastMusic());

        if (indexMusic != actualPlaylist.size() - 1) {
            mediaPlayerManager.startMusic(actualPlaylist.get(indexMusic + 1));
        } else {
            mediaPlayerManager.startMusic(actualPlaylist.get(0));
        }
    }

    public void backMusic(View view) {
        int indexMusic = actualPlaylist.indexOf(playerBarManager.getLastMusic());

        if (indexMusic != 0) {
            mediaPlayerManager.startMusic(actualPlaylist.get(indexMusic - 1));
        } else {
            mediaPlayerManager.startMusic(actualPlaylist.get(actualPlaylist.size() - 1));
        }
    }

    public void loopMusic(View view) {
        mediaPlayerManager.setLooping();
    }

    public void randomMusic(View view) {
        mediaPlayerManager.setRandom();
    }

    public void addToFavs(View view) {
        if (databaseHelperManager.verifyMusicFav(playlistFav, playerBarManager.getLastMusic())) {
            databaseHelperManager.deleteMusicaPlaylist(playlistFav, playerBarManager.getLastMusic());
            playerBarManager.onUnfav();
        } else {
            databaseHelperManager.inserirMusicaPlayList(playlistFav, playerBarManager.getLastMusic());
            playerBarManager.onFav();
        }
    }

    public void removeNotification() {
        if (!mediaPlayerManager.isPlayng()) {
            notificationService.cancelNotification();
        }
    }

    public void AddMusicPlayList(View view) {

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_dialog_add_music_playlist);

        Spinner spinnerPL = dialog.findViewById(R.id.spinnerPlaylists);

        Button btnAdd = dialog.findViewById(R.id.custom_diag_addplay_add);
        Button btnCancel = dialog.findViewById(R.id.custom_diag_addplay_cancel);

        final ArrayList<Playlist> playlistsSpinner = databaseHelperManager.getPlaylists();
        ArrayList<PlayListsSpinner> playListsSpinners = new ArrayList<>();

        for (int i = 0; i < playlistsSpinner.size(); i++) {
            if (!playlistsSpinner.get(i).getNome().equals(getString(R.string.favname))) {
                playListsSpinners.add(new PlayListsSpinner(playlistsSpinner.get(i).getNome(), playlistsSpinner.get(i).getPlaylistID()));
            }
        }

        final SpinnerPlaylistAdapter playlistAdapter;
        playlistAdapter = new SpinnerPlaylistAdapter(this, playListsSpinners);

        spinnerPL.setAdapter(playlistAdapter);
        spinnerPL.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                playlistSelectedAdd = playlistsSpinner.get(position + 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playlistsSpinner.size() > 1) {
                    databaseHelperManager.inserirMusicaPlayList(playlistSelectedAdd, playerBarManager.getLastMusic());
                }
                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        //define o título do Dialog
        dialog.setTitle("Add Playlist");
        dialog.setCancelable(false);
        //exibe na tela o dialog
        dialog.show();
    }

    public void expandPlayer(View view) {
        if (playerBarExpanded.getVisibility() == View.INVISIBLE) {
            playerBarExpanded.setVisibility(View.VISIBLE);
            checkKeyboard();
            playerBarExpanded.startAnimation(fadeIn);
        } else {
            playerBarExpanded.setVisibility(View.INVISIBLE);
            playerBarExpanded.startAnimation(fadeOut);
        }
    }

    private void checkKeyboard(){
        View view = this.getCurrentFocus();
        if (view != null){
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 1:
                if (resultCode == Activity.RESULT_OK) {
                    int result = data.getIntExtra("result", 0);

                    if (result == 1) {
                        playerBarExpanded.setVisibility(View.INVISIBLE);
                        Playlist playlist = (Playlist) data.getSerializableExtra("playlist");
                        playlistManager.prepareRecycle(playlist, musics);
                    }
                }
                break;
        }
    }

    private void prepareToSelect() {
        searchmainicon.setVisible(!searchmainicon.isVisible());
        deletemainicon.setVisible(!deletemainicon.isVisible());
    }

    private void deleteMusicSelecteds() {

        for (int i = 0; i < adapter.selectedItems.size(); i++) {
            int rows = getContentResolver().delete(Uri.parse(musics.get(adapter.selectedItems.keyAt(i)).getUrl()), null, null);
            //int rows = this.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MediaStore.Audio.Media._ID + "=" + musics.get(adapter.selectedItems.keyAt(i)).getMusicID(), null);

            if (rows != 0) {

                if (musics.get(adapter.selectedItems.keyAt(i)).getMusicID().equals(playerBarManager.getLastMusic().getMusicID())) {
                    mediaPlayerManager.stopSystem();
                    playerBarView.setVisibility(View.GONE);
                }

                databaseHelperManager.deleteMusicAllPlaylists(musics.get(adapter.selectedItems.keyAt(i)));
            }
        }

        adapter.selectedItems.clear();
        musics.clear();
        prepareToSelect();
        adapter.notifyDataSetChanged();
        loadMusics();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        searchmainicon = menu.findItem(R.id.menu_item_search);
        deletemainicon = menu.findItem(R.id.menu_item_delete);
        SearchView searchView = (SearchView) searchmainicon.getActionView();
        searchView.setQueryHint(getString(R.string.act_main_searchmusichint));
        searchView.setOnQueryTextListener(this);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }

        if (item.getItemId() == R.id.menu_item_delete) {
            deleteMusicSelecteds();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {

        String userInput = newText.toLowerCase();
        ArrayList<Music> newList = new ArrayList<Music>();

        for (Music m : musics) {
            if (m.getNome().toLowerCase().contains(userInput)) {
                newList.add(m);
            }
        }

        adapter.updateList(newList);
        return false;
    }

    @Override
    public void onBackPressed() {
        if (playerBarExpanded.getVisibility() == View.VISIBLE) {
            playerBarExpanded.setVisibility(View.INVISIBLE);
            playerBarExpanded.startAnimation(fadeOut);
        } else if (playlistView.getVisibility() == View.VISIBLE) {
            playlistView.setVisibility(View.INVISIBLE);
            playlistView.startAnimation(fadeOut);
        } else {
            super.onBackPressed();
        }
    }

    public boolean getIsPlayngMedia() {
        return mediaPlayerManager.isPlayng();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadMusics();
            } else {
                createDialogInfo(getString(R.string.act_main_permissao_negada), getString(R.string.act_main_permissao_negada_message), getString(R.string.act_main_permissao_negada_btn_positive), getString(R.string.act_main_permissao_negada_btn_negative));
            }
        }
    }

    @Override
    protected void onDestroy() {
        unbindService(connection);
        notificationService.stopForeground(true);
        mediaPlayerManager.stopSystem();
        super.onDestroy();
    }

    private void createDialogInfo(String titulo, String mensagem, String btnPos, String btnNeg) {

        final Dialog dialog = new Dialog(this);

        dialog.setContentView(R.layout.custom_dialog_infos);

        TextView titleview = dialog.findViewById(R.id.custom_info_dialog_title);
        TextView messageview = dialog.findViewById(R.id.custom_info_dialog_message);

        Button btnCreate = dialog.findViewById(R.id.custom_info_dialog_btnPositive);
        Button btnCancel = dialog.findViewById(R.id.custom_info_dialog_btnNegative);

        titleview.setText(titulo);
        messageview.setText(mensagem);

        btnCreate.setText(btnPos);
        btnCancel.setText(btnNeg);

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                verifyPermission();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish();
            }
        });

        //define o título do Dialog
        dialog.setTitle("PERMISSION");
        dialog.setCancelable(false);
        //exibe na tela o dialog
        dialog.show();

    }

    //playerBarView.getLayoutParams().height = FrameLayout.LayoutParams.MATCH_PARENT; // LayoutParams: android.view.ViewGroup.LayoutParams
    //playerBarView.requestLayout();
}

