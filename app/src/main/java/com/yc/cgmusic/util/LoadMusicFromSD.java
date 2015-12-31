package com.yc.cgmusic.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.yc.cgmusic.model.Media;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/12/31.
 */
public class LoadMusicFromSD {

    public List<Media> loadMediaList(Context context){
        List<Media> mediaList = new ArrayList<Media>();
        CharacterParser characterParser = new CharacterParser();
        //从SD卡中获取音乐列表
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,null,null,MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (cursor!=null&&cursor.getCount()>0){
            for (int i = 0;i<cursor.getCount();i++){
                cursor.moveToNext();
                if (cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE))>=500*1024
                        &&cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC))==1){
                    String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                    String uri = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    String singer = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    int duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                    String id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                    int album_id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                    //得到media信息
                    Media m = new Media();
                    m.setName(name);
                    m.setUri(uri);
                    m.setSinger(singer);
                    m.setDuration(duration);
                    m.setId(id);
                    m.setAlbum_id(album_id);
                    //将汉字转换成拼音
                    String key = characterParser.getSelling(name);
                    String sortString = key.substring(0, 1).toUpperCase();
                    m.setKey(sortString.toUpperCase());
                    mediaList.add(m);
                }
            }
        }
        cursor.close();
        return mediaList;
    }

}
