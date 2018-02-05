package com.framgia.framgia_broadcast_service_noti;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.concurrent.TimeUnit;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;


public class PlayerActivity extends AppCompatActivity{

    private static final String TAG = "PlayerActivity";
    private SongModel songModel;
    private FloatingActionButton mPlayButton;
    private MusicService mMusicService;
    private boolean mUserIsSeeking = false;
    private boolean mBound = false;
    private SeekBar mSeekBar;
    private TextView mTextCurrentTime;
    private TextView mTextMaxTime;
    private ImageView mImage;
    private Toolbar toolbar;
    private TextView tvSong;
    private TextView tvArtist;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        initializeUI();
        initializeSeekbar();
    }

    private void initializeUI() {
        songModel = (SongModel) getIntent().getSerializableExtra("SongCurrent");
        mImage = findViewById(R.id.im_play_mainsong);
        mPlayButton = findViewById(R.id.fb_play_mainsong);
        mSeekBar = findViewById(R.id.sb_mainsong);
        mTextCurrentTime = findViewById(R.id.time_start_mainsong);
        mTextMaxTime = findViewById(R.id.time_end_mainsong);
        toolbar = findViewById(R.id.tb);
        tvSong = findViewById(R.id.tv_name_mainsong);
        tvArtist = findViewById(R.id.tv_artist_mainsong);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        tvSong.setText(songModel.getName());
        tvArtist.setText(songModel.getArtist());

        Glide.with(this).load(R.drawable.ic_music).apply(bitmapTransform(new CropCircleTransformation())).into(mImage);
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MusicService.class);
                intent.setAction(PlayerAdapter.ACTION_PAUSE_PLAY);
                mMusicService.onBind(intent);
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        startService(new Intent(this, MusicService.class));
        bindService(new Intent(this,
                MusicService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        unbindService(mConnection);
        super.onPause();
    }

    private void initializeSeekbar() {
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            int userSelectedPosition = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    userSelectedPosition = progress;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mUserIsSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mUserIsSeeking = false;
                if (mBound) {
                    mMusicService.actionSeekTo(userSelectedPosition);
                }
            }
        });
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mMusicService = ((MusicService.LocalBinder) service).getService();
            mBound = true;
            mMusicService.setPlaybackListener(new PlaybackListener());
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
            mMusicService.setPlaybackListener(null);
        }
    };


    public class PlaybackListener extends PlaybackInfoListener {

        @Override
        void onDurationChanged(int duration) {
            mSeekBar.setMax(duration);
            int second = (int) TimeUnit.MILLISECONDS.toSeconds(duration);
            mTextMaxTime.setText(String.format("%d:%d", second/60, second%60));
            mPlayButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_black_24dp));
        }

        @Override
        void onPlaybackCompleted() {
        }

        @Override
        void onPositionChanged(int position) {
            if (!mUserIsSeeking) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    mSeekBar.setProgress(position, true);
                } else {
                    mSeekBar.setProgress(position);
                }
            }

            long second = TimeUnit.MILLISECONDS.toSeconds(position);
            mTextCurrentTime.setText(String.format("%02d:%02d", second/60, second%60));
        }

        @Override
        void onStateChanged(int state) {
            if (state == State.PLAYING) {
                mPlayButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_black_24dp));
            } else {
                mPlayButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_arrow_black_24dp));
            }
        }

        @Override
        void onLogUpdated(String formattedMessage) {
            Log.d("TAG", "onLogUpdated: " + formattedMessage);
        }
    }

}
