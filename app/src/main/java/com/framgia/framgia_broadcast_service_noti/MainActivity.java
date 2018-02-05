package com.framgia.framgia_broadcast_service_noti;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static ArrayList<SongModel> songModels = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPer();
        loadFile();
        setUI();
    }

    private void requestPer() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions
                        (this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }
    }
    public void loadFile(){
        ContentResolver contentResolver = getContentResolver();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(songUri,null,null,null,null);
        if (cursor != null && cursor.moveToFirst()){
            int songName = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int songArtist = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int idColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
            do {
                String sName = cursor.getString(songName);
                String sArtist = cursor.getString(songArtist);
                long thisId = cursor.getInt(idColumn);
                SongModel songModel = new SongModel(sName,thisId,sArtist);
                songModels.add(songModel);
            }while (cursor.moveToNext());
        }
    }

    public void setUI(){
        RecyclerView recyclerView = findViewById(R.id.rv_song);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        SongAdapterRV adapter = new SongAdapterRV(this,songModels);
        recyclerView.setAdapter(adapter);
    }

}
