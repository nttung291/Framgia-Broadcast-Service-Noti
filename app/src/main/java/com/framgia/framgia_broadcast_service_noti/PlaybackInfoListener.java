package com.framgia.framgia_broadcast_service_noti;

/**
 * Created by sonng266 on 01/02/2018.
 */

public abstract class PlaybackInfoListener {

    interface State {

        int INVALID = -1;
        int PLAYING = 0;
        int PAUSE = 1;
        int RESET = 2;
        int COMPLETED = 3;
    }

    public static String convertStateToString(int state) {
        switch (state) {
            case State.COMPLETED:
                return "COMPLETED";
            case State.INVALID:
                return "INVALID";
            case State.PAUSE:
                return "PAUSE";
            case State.PLAYING:
                return "PLAYING";
            case State.RESET:
                return "RESET";
            default:
                return "N/A";
        }
    }

    void onLogUpdated(String formattedMessage) {
    }

    void onDurationChanged(int duration){
    }

    void onPositionChanged(int position) {
    }

    void onStateChanged(int state) {
    }

    void onPlaybackCompleted() {
    }
}
