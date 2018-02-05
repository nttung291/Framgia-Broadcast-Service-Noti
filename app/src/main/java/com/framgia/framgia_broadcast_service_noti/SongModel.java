package com.framgia.framgia_broadcast_service_noti;

import java.io.Serializable;

/**
 * Created by nttungg on 1/31/18.
 */

public class SongModel implements Serializable {
    private String name;
    private long Id;
    private String artist;

    public SongModel(String name, long id, String artist) {
        this.name = name;
        this.Id = id;
        this.artist = artist;
    }

    public String getName() {
        return name;
    }

    public long getId() {
        return Id;
    }

    public String getArtist() {
        return artist;
    }

}
