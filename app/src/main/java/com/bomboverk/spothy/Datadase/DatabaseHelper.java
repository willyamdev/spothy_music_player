package com.bomboverk.spothy.Datadase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.bomboverk.spothy.MusicAdapter.Music;
import com.bomboverk.spothy.PlaylistsAdapter.Playlist;
import com.bomboverk.spothy.R;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE = "db_spothy";
    private static final String TBPLs = "tb_playlists";
    private static final String TBMLs = "tb_musiclist";
    private static final int VERSAO = 1;

    private Context context;

    public DatabaseHelper(Context context) {
        super(context, DATABASE, null, VERSAO);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String tbcreatetablepls = "CREATE TABLE IF NOT EXISTS " + TBPLs
                + "(id INTEGER PRIMARY KEY,"
                + "nome TEXT NOT NULL)";

        db.execSQL(tbcreatetablepls);

        String tbcreatetablemusiclists = "CREATE TABLE IF NOT EXISTS " + TBMLs
                + "(id INTEGER PRIMARY KEY,"
                + "id_musica INTEGER NOT NULL,"
                + "id_playlist INTEGER NOT NULL)";

        db.execSQL(tbcreatetablemusiclists);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void verifyPlaylistFav() {
        int count = 1;
        Cursor cursor = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM " + TBPLs + " WHERE id = 1", null);

        while (cursor.moveToNext()) {
            count = cursor.getInt(0);
        }

        if (count == 0) {
            Playlist playlistFav = new Playlist();
            playlistFav.setNome(context.getApplicationContext().getResources().getString(R.string.favname));
            inserirPlayList(playlistFav);
        }
    }

    public Playlist getPlaylistFav() {
        Playlist playlist = new Playlist();

        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM " + TBPLs + " WHERE id = 1", null);

        while (cursor.moveToNext()) {
            playlist.setPlaylistID(cursor.getLong(cursor.getColumnIndex("id")));
            playlist.setNome(cursor.getString(cursor.getColumnIndex("nome")));
        }

        Cursor c = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM " + TBMLs + " WHERE id_playlist = " + playlist.getPlaylistID(), null);
        while (c.moveToNext()) {
            playlist.setItensCount(c.getInt(0));
        }

        return playlist;
    }

    public boolean verifyMusicFav(Playlist playlist, Music music) {
        int count = 0;
        Cursor cursor = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM " + TBMLs + " WHERE id_musica = " + music.getMusicID() +" AND id_playlist = " + playlist.getPlaylistID(), null);

        while (cursor.moveToNext()) {
            count = cursor.getInt(0);
        }

        if (count == 1){
            return  true;
        }else{
            return false;
        }
    }


    public void inserirPlayList(Playlist playlist) {
        ContentValues values = new ContentValues();
        values.put("nome", playlist.getNome());
        getWritableDatabase().insert(TBPLs, null, values);
    }

    public void deletePlaylist(Playlist playlist) {
        SQLiteDatabase db = getWritableDatabase();
        String[] args = {String.valueOf(playlist.getPlaylistID())};
        db.delete(TBMLs, "id_playlist=?", args);
        db.delete(TBPLs, "id=?", args);
    }

    public void updatePlaylist(Playlist playlist) {
        ContentValues values = new ContentValues();

        values.put("nome", playlist.getNome());

        String[] idParaAlterar = {playlist.getPlaylistID().toString()};
        getWritableDatabase().update(TBPLs, values, "id=?", idParaAlterar);
    }

    public void inserirMusicaPlayList(Playlist playlist, Music music) {
        if (!verifyMusicFav(playlist, music)){
            ContentValues values = new ContentValues();
            values.put("id_musica", music.getMusicID());
            values.put("id_playlist", playlist.getPlaylistID());
            getWritableDatabase().insert(TBMLs, null, values);
        }
    }

    public void deleteMusicaPlaylist(Playlist playlist, Music music) {
        SQLiteDatabase db = getWritableDatabase();
        String[] args = {playlist.getPlaylistID().toString(), music.getMusicID().toString()};
        db.delete(TBMLs, "id_playlist=? and id_musica=?", args);
    }

    public void deleteMusicAllPlaylists(Music music) {
        SQLiteDatabase db = getWritableDatabase();
        String[] args = {music.getMusicID().toString()};
        db.delete(TBMLs, "id_musica=?", args);
    }

    public ArrayList<Playlist> getPlaylists() {
        ArrayList<Playlist> playlists = new ArrayList<Playlist>();
        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM " + TBPLs, null);

        while (cursor.moveToNext()) {
            Playlist playlist = new Playlist();

            playlist.setPlaylistID(cursor.getLong(cursor.getColumnIndex("id")));
            playlist.setNome(cursor.getString(cursor.getColumnIndex("nome")));

            Cursor c = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM " + TBMLs + " WHERE id_playlist = " + playlist.getPlaylistID(), null);
            while (c.moveToNext()) {
                playlist.setItensCount(c.getInt(0));
            }

            playlists.add(playlist);
        }
        cursor.close();
        return playlists;
    }

    public ArrayList<Long> getPlaylistMusics(Playlist playlist) {
        ArrayList<Long> musicas = new ArrayList<Long>();
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM " + TBMLs + " WHERE id_playlist = " + playlist.getPlaylistID(), null);

        while (c.moveToNext()) {
            musicas.add(c.getLong(c.getColumnIndex("id_musica")));
        }
        c.close();

        return musicas;
    }

}
