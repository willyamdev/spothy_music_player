package com.bomboverk.spothy.SpinnerAdapter;

public class PlayListsSpinner {

  private String nomePlayList;
  private Long idPlaylist;

  public PlayListsSpinner(String nomePlayList, Long idPlaylist) {
    this.nomePlayList = nomePlayList;
    this.idPlaylist = idPlaylist;
  }

  public String getNomePlayList() {
    return nomePlayList;
  }

  public Long getIdPlaylist() {
    return idPlaylist;
  }
}
