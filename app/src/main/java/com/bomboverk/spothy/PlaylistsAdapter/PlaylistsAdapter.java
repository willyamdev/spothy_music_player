package com.bomboverk.spothy.PlaylistsAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bomboverk.spothy.R;

import java.util.ArrayList;

public class PlaylistsAdapter extends RecyclerView.Adapter {

  private ArrayList<Playlist> playlists;
  private Context context;
  private OnItemClickListener listener;

  public PlaylistsAdapter(ArrayList<Playlist> playlists, Context context, OnItemClickListener listener) {
    this.playlists = playlists;
    this.context = context;
    this.listener = listener;
  }

  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(context).inflate(R.layout.celula_playlist, parent, false);
    return new PlaylistViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    PlaylistViewHolder playlistViewHolder = (PlaylistViewHolder) holder;
    Playlist playlist = playlists.get(position);

    playlistViewHolder.txtNomePlayList.setText(playlist.getNome());
    playlistViewHolder.bind(playlists.get(position), listener);
  }

  @Override
  public int getItemCount() {
    return playlists.size();
  }

  public interface OnItemClickListener {
    void onItemClick(Playlist playlist);
  }

  public void updateList(ArrayList<Playlist> filterPlaylists){

    playlists = new ArrayList<Playlist>();
    playlists.addAll(filterPlaylists);

    notifyDataSetChanged();
  }

}
