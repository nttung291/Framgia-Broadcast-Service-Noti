package com.framgia.framgia_broadcast_service_noti;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;


/**
 * Created by nttungg on 1/31/18.
 */

public class SongAdapterRV extends RecyclerView.Adapter<SongAdapterRV.ViewHolder>  {
    private Context context;
    private List<SongModel> songModelList;
    LayoutInflater layoutInflater;


    public SongAdapterRV(Context context, List<SongModel> songModelList) {
        this.context = context;
        this.songModelList = songModelList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (layoutInflater == null){
            layoutInflater = LayoutInflater.from(parent.getContext());
        }
        View itemView = layoutInflater.inflate(R.layout.item_song,parent,false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bindData(songModelList.get(position));
    }

    @Override
    public int getItemCount() {
        return songModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_name;
        private TextView tv_artist;
        private ImageView imageView;
        View view;
        public ViewHolder(View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_artist = itemView.findViewById(R.id.tv_artist);
            imageView = itemView.findViewById(R.id.imageView);
            view = itemView;
        }

        public void bindData(final SongModel songModel){
            if (songModel != null) {
                tv_name.setText(songModel.getName());
                tv_artist.setText(songModel.getArtist());
                Glide.with(context).load(R.drawable.ic_music).apply(bitmapTransform(new CropCircleTransformation())).into(imageView);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intentS = new Intent(context, MusicService.class);
                        intentS.setAction(PlayerAdapter.ACTION_PAUSE_PLAY);
                        intentS.putExtra(PlayerAdapter.FILE_KEY, songModel);
                        context.startService(intentS);

                        Intent intent = new Intent(context,PlayerActivity.class);
                        intent.putExtra("SongCurrent" , songModel);
                        context.startActivity(intent);
                    }
                });
            }
        }
    }
}
