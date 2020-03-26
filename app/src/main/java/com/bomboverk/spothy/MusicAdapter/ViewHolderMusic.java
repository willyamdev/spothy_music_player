package com.bomboverk.spothy.MusicAdapter;

import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bomboverk.spothy.R;

public class ViewHolderMusic extends RecyclerView.ViewHolder {

    TextView nameMusic, nameAutor;
    ImageView playIcon, favIcon;
    public ConstraintLayout constraintLayout;

    public ViewHolderMusic(@NonNull View itemView) {
        super(itemView);

        nameMusic = (TextView) itemView.findViewById(R.id.cel_musica_textNome);
        nameAutor = (TextView) itemView.findViewById(R.id.cel_musica_textAutor);

        playIcon = itemView.findViewById(R.id.cel_music_imagePlay);
        favIcon = itemView.findViewById(R.id.cel_music_imageFav);

        constraintLayout = itemView.findViewById(R.id.cel_musica_constraintBg);
    }

    public void bind(final Music item, final MusicAdapter.OnItemClickListener listener) {

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(item, ViewHolderMusic.this, getAdapterPosition());
            }
        });

        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                listener.OnLongPress(item, ViewHolderMusic.this, getAdapterPosition());
                return true;
            }
        });
    }
}
