package com.framgia.framgia_broadcast_service_noti;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by nttungg on 2/1/18.
 */

public class MediaPlayerHolder implements PlayerAdapter{
    public static final int PLAYBACK_POSITION_REFRESH_INTERVAL_MS = 1000;

    private Context mContext;
    private MediaPlayer mMediaPlayer;
    private PlaybackInfoListener mPlaybackInfoListener;
    private ScheduledExecutorService mExecutor;
    private long mResourceId;
    private Runnable mSeekbarPositionUpdateTask;
    private Handler mHandler;
    private Notification mMusicNotification;
    private SongModel mSong;

    public MediaPlayerHolder(Context context) {
        mContext = context.getApplicationContext();
    }

    private void initializeMediaPlayer() {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopUpdatingCallbackWithPosition(true);
                    if (mPlaybackInfoListener != null) {
                        mPlaybackInfoListener.onStateChanged(PlaybackInfoListener.State.COMPLETED);
                        mPlaybackInfoListener.onPlaybackCompleted();
                    }
                }
            });

        }
    }

    private void stopUpdatingCallbackWithPosition(boolean resetUIPlaybackPosition) {
        if (mExecutor != null) {
            mExecutor.shutdownNow();
            mExecutor = null;
            mSeekbarPositionUpdateTask = null;
            if (resetUIPlaybackPosition && mPlaybackInfoListener != null) {
                mPlaybackInfoListener.onPositionChanged(0);
            }
        }
    }

    private void logToUI(String s) {
        if (mPlaybackInfoListener != null) {
            mPlaybackInfoListener.onLogUpdated(s);
        }
    }

    private void startUpdatingCallbackWithPosition() {
        if (mExecutor == null) {
            mExecutor = Executors.newSingleThreadScheduledExecutor();
        }

        if (mSeekbarPositionUpdateTask == null) {

            mHandler = new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    updateProgressCallbackTask();
                    return false;
                }
            });

            mSeekbarPositionUpdateTask = new Runnable() {
                @Override
                public void run() {
                    if (mHandler != null) {
                        mHandler.sendEmptyMessage(0);
                    }
                }
            };
        }
        mExecutor.scheduleAtFixedRate(mSeekbarPositionUpdateTask,
                0,
                PLAYBACK_POSITION_REFRESH_INTERVAL_MS,
                TimeUnit.MILLISECONDS);
    }

    private void updateProgressCallbackTask() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            int currentPosition = mMediaPlayer.getCurrentPosition();
            if (mPlaybackInfoListener != null) {
                mPlaybackInfoListener.onPositionChanged(currentPosition);
            }
        }
    }

    public void setPlaybackInfoListener(PlaybackInfoListener listener) {
        mPlaybackInfoListener = listener;
        initializeProgresCallback();
    }


    private void initNotification(int state) {
        Intent notificationIntent = new Intent(mContext, MainActivity.class);
        PendingIntent pendingIntentOpenApp = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);

        Intent actionPlayIntent = new Intent(mContext, MusicService.class);
        actionPlayIntent.setAction(PlayerAdapter.ACTION_PAUSE_PLAY);
        PendingIntent ptPlayPause = PendingIntent.getService(mContext, 0, actionPlayIntent, 0);

        Intent actionNextIntent = new Intent(mContext, MusicService.class);
        actionNextIntent.setAction(PlayerAdapter.ACTION_NEXT);
        PendingIntent nextPendingIntent = PendingIntent.getService(mContext, 0, actionNextIntent, 0);

        Intent actionPrevIntent = new Intent(mContext, MusicService.class);
        actionPrevIntent.setAction(PlayerAdapter.ACTION_PREV);
        PendingIntent prevPendingIntent = PendingIntent.getService(mContext, 0, actionPrevIntent, 0);

        if (state == PlaybackInfoListener.State.PLAYING) {
            mMusicNotification = new NotificationCompat.Builder(mContext, "")
                    // Show controls on lock screen even when user hides sensitive content.
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setSmallIcon(R.drawable.ic_play_arrow_black_24dp)
                    .setAutoCancel(false)
                    .setOngoing(true)
                    // Add media control buttons that invoke intents in your media service
                    .setContentIntent(pendingIntentOpenApp)
                    .addAction(R.drawable.ic_skip_previous_black_24dp, "Previous", prevPendingIntent) // #0
                    .addAction(R.drawable.ic_pause_black_24dp, "Pause", ptPlayPause)  // #1
                    .addAction(R.drawable.ic_skip_next_black_24dp, "Next", nextPendingIntent)     // #2
                    // Apply the media style template
                    .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                            .setShowActionsInCompactView(0, 1, 2))
                    .setContentTitle(mSong.getName())
                    .setContentText(mSong.getArtist())
                    .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_music))
                    .build();
        } else {
            mMusicNotification = new NotificationCompat.Builder(mContext, "")
                    // Show controls on lock screen even when user hides sensitive content.
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setSmallIcon(R.drawable.ic_play_arrow_black_24dp)
                    .setAutoCancel(false)
                    .setOngoing(false)
                    // Add media control buttons that invoke intents in your media service
                    .setContentIntent(pendingIntentOpenApp)
                    .addAction(R.drawable.ic_skip_previous_black_24dp, "Previous", prevPendingIntent) // #0
                    .addAction(R.drawable.ic_play_arrow_black_24dp, "Pause", ptPlayPause)  // #1
                    .addAction(R.drawable.ic_skip_next_black_24dp, "Next", nextPendingIntent)     // #2
                    // Apply the media style template
                    .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                            .setShowActionsInCompactView(0, 1, 2))
                    .setContentTitle(mSong.getName())
                    .setContentText(mSong.getArtist())
                    .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_music))
                    .build();
        }

        NotificationManager notificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(
                PlayerAdapter.NOTIFY_ID, mMusicNotification);
    }

    @Override
    public void loadMedia(SongModel songModel) {
        mSong = songModel;
        mResourceId = songModel.getId();

        initializeMediaPlayer();
        Uri contentUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mResourceId);
        try {
            mMediaPlayer.setDataSource(mContext.getApplicationContext(), contentUri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            mMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        play();
        initializeProgresCallback();
    }

    @Override
    public void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public boolean isPlaying() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.isPlaying();
        }
        return false;
    }

    @Override
    public void play() {
        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            logToUI(String.format("playbackStart() %s", mResourceId));
            mMediaPlayer.start();
            if (mPlaybackInfoListener != null) {
                mPlaybackInfoListener.onStateChanged(PlaybackInfoListener.State.PLAYING);
            }
            initNotification(PlaybackInfoListener.State.PLAYING);
            startUpdatingCallbackWithPosition();
        }
    }

    @Override
    public void reset() {
        if (mMediaPlayer != null) {
            logToUI("playbackReset()");
            mMediaPlayer.reset();
            loadMedia(mSong);
            if (mPlaybackInfoListener != null) {
                mPlaybackInfoListener.onStateChanged(PlaybackInfoListener.State.RESET);
            }
            stopUpdatingCallbackWithPosition(true);
            play();
        }
    }

    @Override
    public void pause() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            if (mPlaybackInfoListener != null) {
                mPlaybackInfoListener.onStateChanged(PlaybackInfoListener.State.PAUSE);
            }
            initNotification(PlaybackInfoListener.State.PAUSE);
        }
    }

    @Override
    public void initializeProgresCallback() {
        if (mMediaPlayer != null && mPlaybackInfoListener != null) {
            int duration = mMediaPlayer.getDuration();
            mPlaybackInfoListener.onDurationChanged(duration);
            mPlaybackInfoListener.onPositionChanged(mMediaPlayer.getCurrentPosition());
        }
    }


    @Override
    public void seekTo(int position) {
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(position);
        }
    }

    @Override
    public Notification getMusicNotification() {
        if (mMusicNotification == null) {
            initNotification(PlaybackInfoListener.State.PLAYING);
        }

        return mMusicNotification;
    }
}
