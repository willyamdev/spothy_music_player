package com.bomboverk.spothy.PlaylistsAdapter;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bomboverk.spothy.Ads.AdsManager;
import com.bomboverk.spothy.Datadase.DatabaseHelper;
import com.bomboverk.spothy.MainActivity;
import com.bomboverk.spothy.MusicAdapter.Music;
import com.bomboverk.spothy.MusicAdapter.MusicAdapter;
import com.bomboverk.spothy.MusicAdapter.ViewHolderMusic;
import com.bomboverk.spothy.R;

import java.util.ArrayList;

public class PlaylistManager {

    private Context context;
    private Playlist playlist;
    private FrameLayout playlistLayout;
    private ArrayList<Music> musicas;

    private TextView playlistName;
    private TextView musicCount;

    private ImageButton deletePL;
    private ImageButton editPL;
    private ImageButton deleteMusicPl;

    private ImageView iconPlaylist;

    RecyclerView plMusicsRecycle;
    MusicAdapter adapter;

    MainActivity main;
    DatabaseHelper database;

    ArrayList<Music> thisPlaylistMusic;

    private boolean mode = false;

    private AdsManager adsManager;

    public PlaylistManager(Context context, final FrameLayout playlistLayout, final MainActivity main) {
        this.context = context;
        this.playlistLayout = playlistLayout;
        this.main = main;

        playlistName = playlistLayout.findViewById(R.id.lay_pl_name);
        musicCount = playlistLayout.findViewById(R.id.lay_pl_musiccount);

        deletePL = playlistLayout.findViewById(R.id.lay_pl_btndelete);
        editPL = playlistLayout.findViewById(R.id.lay_pl_btnedit);
        deleteMusicPl = playlistLayout.findViewById(R.id.lay_pl_btnDelMusic);

        plMusicsRecycle = playlistLayout.findViewById(R.id.lay_pl_recycle);

        iconPlaylist = playlistLayout.findViewById(R.id.lay_pl_imageIcon);

        database = new DatabaseHelper(context);

        deletePL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                database.deletePlaylist(playlist);
                playlistLayout.setVisibility(View.INVISIBLE);
            }
        });

        editPL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exibeDialogGDInfo();
            }
        });

        deleteMusicPl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAllSelecteds();
            }
        });

        adsManager = new AdsManager(context);
    }

    public void prepareRecycle(Playlist playlist, ArrayList<Music> musicas) {
        this.playlist = null;
        this.playlist = playlist;

        this.musicas = new ArrayList<Music>();
        this.musicas.addAll(musicas);

        adsManager.createInterstical(context);

        loadLayout();
    }

    public void loadLayout() {
        String quant = Integer.toString(playlist.getItensCount());

        playlistName.setText(playlist.getNome());
        musicCount.setText(context.getApplicationContext().getResources().getString(R.string.plm_music_count)+" "+quant);

        if (playlist.getNome().equals(context.getApplicationContext().getResources().getString(R.string.favname))) {
            mode = true;
            editPL.setVisibility(View.INVISIBLE);
            deletePL.setVisibility(View.INVISIBLE);
            iconPlaylist.setImageResource(R.drawable.ic_playlist_fav);
        } else {
            mode = false;
            editPL.setVisibility(View.VISIBLE);
            deletePL.setVisibility(View.VISIBLE);
            iconPlaylist.setImageResource(R.drawable.ic_playlist_user);
        }

        playlistLayout.setVisibility(View.VISIBLE);

        loadPlaylistMusics();
    }

    private void loadPlaylistMusics() {
        ArrayList<Long> musicsList;
        musicsList = database.getPlaylistMusics(playlist);
        database.close();

        thisPlaylistMusic = new ArrayList<Music>();

        if (musicsList.size() > 0) {

            for (int i = 0; i < musicsList.size(); i++) {
                for (int a = 0; a < musicas.size(); a++) {
                    if (musicsList.get(i).equals(musicas.get(a).getMusicID())) {
                        thisPlaylistMusic.add(musicas.get(a));
                        break;
                    }
                }
            }

            if (adapter != null && adapter.getItemCount() > 0) {
                adapter.clearRecycle();
            }

            adapter = new MusicAdapter(thisPlaylistMusic, main, mode, new MusicAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(Music music, ViewHolderMusic vholder, int position) {

                    if (adapter.selectedItems.size() > 0){

                        if (adapter.selectedItems.get(position, false)) {
                            adapter.selectedItems.delete(position);
                            vholder.constraintLayout.setSelected(false);
                        }else {
                            adapter.selectedItems.put(position, true);
                            vholder.constraintLayout.setSelected(true);
                        }

                        if (adapter.selectedItems.size() == 0){
                            prepareToSelect(false);
                        }

                    }else{
                        main.startSpecificMusic(music, thisPlaylistMusic);
                    }
                }

                @Override
                public void OnLongPress(Music music, ViewHolderMusic vholder, int position) {
                    if (!adapter.selectedItems.get(position, false)) {
                        if (adapter.selectedItems.size() == 0) {
                            prepareToSelect(true);
                        }

                        adapter.selectedItems.put(position, true);
                        vholder.constraintLayout.setSelected(true);
                    }
                }
            });

            plMusicsRecycle.setAdapter(adapter);
            plMusicsRecycle.setVisibility(View.VISIBLE);

            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(main, LinearLayoutManager.VERTICAL, false);
            plMusicsRecycle.setLayoutManager(layoutManager);
        } else {
            if (adapter != null && adapter.getItemCount() > 0) {
                adapter.clearRecycle();
            }
        }
    }

    private void prepareToSelect(boolean removeMode) {
        if (removeMode) {
            deleteMusicPl.setVisibility(View.VISIBLE);
        } else {
            deleteMusicPl.setVisibility(View.INVISIBLE);
        }
    }

    private void deleteAllSelecteds() {

        for (int i = 0; i < adapter.selectedItems.size(); i++){
            database.deleteMusicaPlaylist(playlist, thisPlaylistMusic.get(adapter.selectedItems.keyAt(i)));
        }

        adapter.selectedItems.clear();
        prepareToSelect(false);
        adapter.notifyDataSetChanged();
        loadPlaylistMusics();
    }


    public void exibeDialogGDInfo() {
        final Dialog dialog = new Dialog(main);

        dialog.setContentView(R.layout.custom_dialog_create_playlist);

        TextView titleview = dialog.findViewById(R.id.custom_diag_titleText);
        titleview.setText(context.getResources().getString(R.string.pml_dg_title));

        final EditText inputPlaylistName = dialog.findViewById(R.id.custom_diag_editTextPName);

        inputPlaylistName.setText(playlist.getNome());

        Button btnCreate = dialog.findViewById(R.id.custom_diag_btnCreate);
        Button btnCancel = dialog.findViewById(R.id.custom_diag_btnCancel);
        btnCreate.setText(context.getResources().getString(R.string.pml_dg_btn_att));

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!inputPlaylistName.getText().toString().equals("")) {

                    String getPlaylistName = inputPlaylistName.getText().toString();
                    playlist.setNome(getPlaylistName);

                    database.updatePlaylist(playlist);
                    playlistName.setText(getPlaylistName);

                    dialog.dismiss();
                } else {
                    dialog.dismiss();
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
        dialog.setTitle("Edit Playlist");
        dialog.setCancelable(false);
        //exibe na tela o dialog
        dialog.show();
    }

    //adapter.selectedItems.keyAt(i); VALORES = 1,2,3,4
    //adapter.selectedItems.get(adapter.selectedItems.keyAt(i)) VALORES = TRUE OU FALSE

}
