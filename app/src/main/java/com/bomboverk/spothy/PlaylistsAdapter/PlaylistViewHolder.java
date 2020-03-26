package com.bomboverk.spothy.PlaylistsAdapter;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bomboverk.spothy.R;

public class PlaylistViewHolder extends RecyclerView.ViewHolder {


  TextView txtNomePlayList;

  public PlaylistViewHolder(@NonNull View itemView) {
    super(itemView);

    txtNomePlayList = itemView.findViewById(R.id.cel_playlist_txtName);
  }


  public void bind(final Playlist item, final PlaylistsAdapter.OnItemClickListener listener) {
    itemView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        listener.onItemClick(item);
      }
    });
  }

}
