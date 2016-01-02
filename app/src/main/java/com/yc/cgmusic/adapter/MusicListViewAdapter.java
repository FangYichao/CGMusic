package com.yc.cgmusic.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yc.cgmusic.R;
import com.yc.cgmusic.model.Media;

import java.util.List;

/**
 * Created by Administrator on 2015/12/31.
 */
public class MusicListViewAdapter extends BaseAdapter {

    private List<Media> medias;
    private Context context;
    private int resourceId;

    private ContentResolver contentResolver;

    private LayoutInflater layoutInflater;

    public MusicListViewAdapter(Context context,int textViewResourceId, List<Media> medias){

        this.medias = medias;
        this.context = context;
        resourceId = textViewResourceId;
        layoutInflater = LayoutInflater.from(context);
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

        ViewHolde viewHolde = null;
        if (convertView == null){
            convertView = layoutInflater.inflate(resourceId,null);
            viewHolde = new ViewHolde();
            viewHolde.songId = (TextView) convertView.findViewById(R.id.listView_item_id);
            viewHolde.songName = (TextView) convertView.findViewById(R.id.listView_item_songName);
            viewHolde.singer = (TextView) convertView.findViewById(R.id.listView_item_singer);
            viewHolde.songTime = (TextView) convertView.findViewById(R.id.listView_item_time);
            viewHolde.catalog = (TextView) convertView.findViewById(R.id.listView_catalog);
            viewHolde.ziMu = (LinearLayout) convertView.findViewById(R.id.listView_layout);
            convertView.setTag(viewHolde);
        }else {
            viewHolde = (ViewHolde) convertView.getTag();
        }
        // 根据position获取分类的首字母的Char ascii值
        int section = getSectionForPosition(position);
        if (position == getPositionForSection(section)) {
            viewHolde.catalog.setVisibility(View.VISIBLE);

            viewHolde.ziMu.setVisibility(View.VISIBLE);
            viewHolde.catalog.setText(medias.get(position).getKey());
        } else {
            viewHolde.catalog.setVisibility(View.GONE);
            viewHolde.ziMu.setVisibility(View.GONE);
        }


        viewHolde.songName.setText(medias.get(position).getName());
        viewHolde.songId.setText(""+(position+1)+".");
        viewHolde.singer.setText(medias.get(position).getSinger());
        viewHolde.songTime.setText(timeconvert(medias.get(position).getDuration()));

        return convertView;
    }
    public static class ViewHolde{
        public TextView songName,songId,singer, songTime, catalog;
        public LinearLayout ziMu;
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
    public int getPositionForSection(int section) {
        for (int i = 0; i < getCount(); i++) {
            String sortStr = medias.get(i).getKey();
            char firstChar = sortStr.toUpperCase().charAt(0);
            if (firstChar == section) {
                return i;
            }
        }
        return -1;
    }
    public int getSectionForPosition(int position) {
        return medias.get(position).getKey().charAt(0);
    }



}
