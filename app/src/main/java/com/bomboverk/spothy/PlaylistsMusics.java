package com.bomboverk.spothy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;

import com.bomboverk.spothy.Ads.AdsManager;
import com.bomboverk.spothy.Datadase.DatabaseHelper;
import com.bomboverk.spothy.PlaylistsAdapter.Playlist;
import com.bomboverk.spothy.PlaylistsAdapter.PlaylistsAdapter;

import java.util.ArrayList;

public class PlaylistsMusics extends AppCompatActivity implements SearchView.OnQueryTextListener{

    Toolbar mToolBar;

    RecyclerView playListRecycle;

    ArrayList<Playlist> playlists;
    PlaylistsAdapter adapter;

    private MenuItem searchmainicon;

    int result = 0;
    Playlist selectedPlaylist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlists);

        mToolBar = findViewById(R.id.act_playlist_toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        playListRecycle = findViewById(R.id.act_playlists_recycle);

        loadRecycle();
    }

    private void loadRecycle(){
        DatabaseHelper database = new DatabaseHelper(getApplicationContext());
        playlists = database.getPlaylists();
        database.close();

        adapter = new PlaylistsAdapter(playlists, getApplicationContext(), new PlaylistsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Playlist playlist) {
                result = 1;
                selectedPlaylist = playlist;
                finish();
            }
        });

        playListRecycle.setAdapter(adapter);

        playListRecycle.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                playListRecycle.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int width = playListRecycle.getWidth();
                calculateNoOfColumns(116, width);
            }
        });

    }

    public void calculateNoOfColumns(float columnWidthDp, int rVWidth) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float screenWidthDp = rVWidth / displayMetrics.density;
        int noOfColumns = (int) (screenWidthDp / columnWidthDp + 0.5); // +0.5 for correct rounding to int.

        GridLayoutManager layoutManager = new GridLayoutManager(this, noOfColumns);
        //StaggeredGridLayoutManager gridLayoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        playListRecycle.setLayoutManager(layoutManager);
    }

    private void reloadRecycle(){
        playlists.clear();
        adapter.notifyDataSetChanged();

        loadRecycle();
    }

    public void exibeDialogGDInfo(View view) {
        final Dialog dialog = new Dialog(this);

        dialog.setContentView(R.layout.custom_dialog_create_playlist);

        final EditText inputPlaylistName = dialog.findViewById(R.id.custom_diag_editTextPName);

        Button btnCreate = dialog.findViewById(R.id.custom_diag_btnCreate);
        Button btnCancel = dialog.findViewById(R.id.custom_diag_btnCancel);

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!inputPlaylistName.getText().toString().equals("")){

                    String getPlaylistName = inputPlaylistName.getText().toString();
                    Playlist newPlaylist = new Playlist();
                    newPlaylist.setNome(getPlaylistName);

                    DatabaseHelper database = new DatabaseHelper(getApplicationContext());
                    database.inserirPlayList(newPlaylist);
                    database.close();

                    reloadRecycle();
                    dialog.dismiss();
                }else {
                    Toast.makeText(getApplicationContext(), getString(R.string.act_plmusic_error), Toast.LENGTH_SHORT).show();
                }

            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        //define o título do Dialog
        dialog.setTitle("Create Playlist");
        dialog.setCancelable(false);
        //exibe na tela o dialog
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        searchmainicon = menu.findItem(R.id.menu_item_search);
        SearchView searchView = (SearchView) searchmainicon.getActionView();
        searchView.setQueryHint(getString(R.string.act_plmusic_hint_search));
        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
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
        ArrayList<Playlist> newList = new ArrayList<Playlist>();

        for (Playlist p : playlists) {
            if (p.getNome().toLowerCase().contains(userInput)) {
                newList.add(p);
            }
        }

        adapter.updateList(newList);
        return false;
    }

    @Override
    public void finish() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("result",result);
        returnIntent.putExtra("playlist",selectedPlaylist);
        setResult(Activity.RESULT_OK,returnIntent);
        super.finish();
    }
}
