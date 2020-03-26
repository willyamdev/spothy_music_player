package com.bomboverk.spothy.MusicAdapter;


public class Music{

    private String nome, artista, url;
    private Long musicID;

    public Music(String nome, String artista, String url, Long musicID) {
        this.nome = nome;
        this.artista = artista;
        this.url = url;
        this.musicID = musicID;
    }

    public String getNome() {
        return nome;
    }

    public String getArtista() {
        return artista;
    }

    public String getUrl() {
        return url;
    }

    public Long getMusicID() {
        return musicID;
    }
}
