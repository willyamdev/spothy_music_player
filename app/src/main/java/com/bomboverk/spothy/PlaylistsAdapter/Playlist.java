package com.bomboverk.spothy.PlaylistsAdapter;

import java.io.Serializable;

public class Playlist implements Serializable {

  private Long playlistID;
  private String nome;
  private int itensCount;

  public Long getPlaylistID() {
    return playlistID;
  }

  public void setPlaylistID(Long playlistID) {
    this.playlistID = playlistID;
  }

  public String getNome() {
    return nome;
  }

  public void setNome(String nome) {
    this.nome = nome;
  }

  public int getItensCount() {
    return itensCount;
  }

  public void setItensCount(int itensCount) {
    this.itensCount = itensCount;
  }
}
