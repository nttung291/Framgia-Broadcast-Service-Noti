package com.framgia.framgia_broadcast_service_noti;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class MusicService extends Service {
    private final IBinder mBinder = new LocalBinder();

    private PlayerAdapter mPlayerAdapter;
    private boolean mHavingASong = false;

    public MusicService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initializePlaybackControl();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleIntent(intent);
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        handleIntent(intent);
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (mPlayerAdapter.isPlaying()) {
            startForeground(PlayerAdapter.NOTIFY_ID, mPlayerAdapter.getMusicNotification());
        }
        return super.onUnbind(intent);
    }

    public void handleIntent(Intent intent) {
        String action = "";

        if (intent != null && intent.getAction() != null) {
            action = intent.getAction();
        } else {
            if (intent == null && !mPlayerAdapter.isPlaying()) {
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(PlayerAdapter.NOTIFY_ID);
            }
        }

        switch (action) {
            case PlayerAdapter.ACTION_PAUSE_PLAY:
                SongModel songModel = (SongModel) intent.getSerializableExtra(PlayerAdapter.FILE_KEY);
                actionPlayPauseMusic(songModel);
                break;
            default:
                break;
        }
    }

    private void actionPlayPauseMusic(SongModel songModel) {
        if (songModel != null) {
            mPlayerAdapter.release();
            mPlayerAdapter.loadMedia(songModel);
            mHavingASong = true;
        } else {
            if (mPlayerAdapter.isPlaying()) {
                stopForeground(true);
                mPlayerAdapter.pause();
            } else if (mHavingASong) {
                mPlayerAdapter.play();
            } else {
                Toast.makeText(this, "No Song Selected!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initializePlaybackControl() {
        mPlayerAdapter = new MediaPlayerHolder(this);
    }

    public void setPlaybackListener(PlaybackInfoListener playbackListener) {
        if (mPlayerAdapter != null) {
            ((MediaPlayerHolder)mPlayerAdapter).setPlaybackInfoListener(playbackListener);
        }
    }

    public void actionSeekTo(int position) {
        mPlayerAdapter.seekTo(position);
    }

    class LocalBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }
}
