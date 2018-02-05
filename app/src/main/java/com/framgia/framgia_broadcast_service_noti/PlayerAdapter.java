package com.framgia.framgia_broadcast_service_noti;

import android.app.Notification;

/**
 * Created by sonng266 on 01/02/2018.
 */

public interface PlayerAdapter {

    int NOTIFY_ID = 1;
    String FILE_KEY = "file_key";
    String ACTION_PAUSE_PLAY = "action_play";
    String ACTION_NEXT = "action_next";
    String ACTION_PREV = "action_prev";

    void loadMedia(SongModel songModel);

    void release();

    boolean isPlaying();

    void play();

    void reset();

    void pause();

    void initializeProgresCallback();

    void seekTo(int position);

    Notification getMusicNotification();
}
