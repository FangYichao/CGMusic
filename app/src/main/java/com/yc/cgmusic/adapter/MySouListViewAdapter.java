package com.yc.cgmusic.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yc.cgmusic.R;
import com.yc.cgmusic.model.Media;

import java.util.List;

/**
 * Created by Administrator on 2016/1/4.
 */
public class MySouListViewAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private List<Media> medias;
    private Context context;
    public MySouListViewAdapter(Context context, List<Media> medias) {
        this.medias = medias;
        this.context = context;
        mInflater = LayoutInflater.from(context);
    }
    @Override
    public int getCount() {
        return medias == null ? 0 : medias.size();
    }

    @Override
    public Object getItem(int position) {
        return medias.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.sou_list_item, null);
            holder = new ViewHolder();
            holder.songId = (TextView) convertView.findViewById(R.id.sou_item_id);
            holder.songName = (TextView) convertView.findViewById(R.id.sou_item_songName);
            holder.singer = (TextView) convertView.findViewById(R.id.sou_item_singer);
            holder.songTime = (TextView) convertView.findViewById(R.id.sou_item_time);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        holder.songName.setText(medias.get(position).getName());
        holder.songId.setText("" + (position + 1) + ".");
        holder.singer.setText(medias.get(position).getSinger());
        holder.songTime.setText(timeconvert(medias.get(position).getDuration()));
        return convertView;
    }
    private class ViewHolder {
        public TextView songName, songId, singer, songTime;
    }
    /**
     *歌曲时间转换
     */
    private String timeconvert(int time) {
        int min =0;
        String minText,secondText;
        time /= 1000;
        min = time / 60;
        time %= 60;
        if (min>=10){
            minText = ""+min;
        }else {
            minText = "0"+min+"";
        }
        if (time>=10){
            secondText = ""+time;
        }else {
            secondText = "0"+time+"";
        }
        return minText + ":" + secondText;
    }
}
