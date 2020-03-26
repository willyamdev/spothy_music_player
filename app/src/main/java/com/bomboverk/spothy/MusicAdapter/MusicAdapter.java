package com.bomboverk.spothy.MusicAdapter;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bomboverk.spothy.R;

import java.util.ArrayList;

public class MusicAdapter extends RecyclerView.Adapter {

    private ArrayList<Music> musicas;
    private Context context;
    private boolean mode;
    private OnItemClickListener listener;

    public SparseBooleanArray selectedItems;


    public MusicAdapter(ArrayList<Music> musicas, Context context, boolean mode, OnItemClickListener listener) {
        this.musicas = musicas;
        this.mode = mode;
        this.context = context;
        this.listener = listener;
        selectedItems = new SparseBooleanArray();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.celula_musica, parent, false);
        return new ViewHolderMusic(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        ViewHolderMusic meuViewHolder = (ViewHolderMusic) holder;
        Music music = musicas.get(position);

        meuViewHolder.nameMusic.setText(music.getNome());
        meuViewHolder.nameAutor.setText(music.getArtista());

        meuViewHolder.constraintLayout.setSelected(selectedItems.get(position, false));

        meuViewHolder.bind(musicas.get(position), listener);

        if (mode){
            meuViewHolder.favIcon.setVisibility(View.VISIBLE);
            meuViewHolder.playIcon.setVisibility(View.INVISIBLE);
        }else{
            meuViewHolder.favIcon.setVisibility(View.INVISIBLE);
            meuViewHolder.playIcon.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return musicas.size();
    }

    public interface OnItemClickListener{
        void onItemClick(Music music, ViewHolderMusic vholder, int position);
        void OnLongPress(Music music, ViewHolderMusic vholder , int position);
    }

    public void updateList(ArrayList<Music> filterMusics){
        musicas = new ArrayList<Music>();
        musicas.addAll(filterMusics);
        notifyDataSetChanged();
    }

    public void clearRecycle(){
        musicas = new ArrayList<Music>();
        notifyDataSetChanged();
    }

}
