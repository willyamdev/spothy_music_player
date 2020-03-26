package com.bomboverk.spothy.SpinnerAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bomboverk.spothy.R;

import java.util.ArrayList;

public class SpinnerPlaylistAdapter extends ArrayAdapter<PlayListsSpinner> {


  public SpinnerPlaylistAdapter(Context context, ArrayList<PlayListsSpinner> rdSpinnerItens){
    super(context, 0, rdSpinnerItens);
  }

  @NonNull
  @Override
  public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    return initView(position, convertView, parent);
  }

  @Override
  public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    return initView(position, convertView, parent);
  }

  private View initView(int position, View convertView, ViewGroup parent){

    if (convertView==null){
      convertView = LayoutInflater.from(getContext()).inflate(R.layout.spinner_celula_playlists, parent, false);
    }

    TextView txt = convertView.findViewById(R.id.spinnerItem);

    PlayListsSpinner currentItem = getItem(position);

    if (currentItem != null){
      txt.setText(currentItem.getNomePlayList());
    }
    return convertView;
  }

}
