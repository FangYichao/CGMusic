package com.yc.cgmusic.activity;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.yc.cgmusic.R;
import com.yc.cgmusic.adapter.MySouListViewAdapter;
import com.yc.cgmusic.model.Media;
import com.yc.cgmusic.model.MyConstant;
import com.yc.cgmusic.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 搜索.
 */
public class SearchActivity extends ListActivity implements AdapterView.OnItemClickListener {
    private List<Media> medias_search;
    private ListView lv_search;
    private CustomAsyncQueryHandler asyncQueryHandler;
    private MySouListViewAdapter listViewAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lv_search = getListView();
        lv_search.setBackgroundResource(R.mipmap.image_beijin);
        medias_search = new ArrayList<Media>();
        handleIntent(getIntent());

    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }
    private void handleIntent(Intent intent){
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doMySearch(query);
        }

    }
    /**
     * 搜索实现
     */
    private void doMySearch(String query) {

        LogUtil.e("5765756756","3135432313");
        asyncQueryHandler = new CustomAsyncQueryHandler(getContentResolver());
        asyncQueryHandler.startQuery(0, lv_search, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, "title like ?",
                new String[] { "%" + query + "%" }, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        lv_search.setOnItemClickListener(this);
    }



    /**
     * 异步查询处理器
     */
    private class CustomAsyncQueryHandler extends AsyncQueryHandler {

        public CustomAsyncQueryHandler(ContentResolver cr) {
            super(cr);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {

            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToNext();

                // 如果当前媒体库的文件大于500Kb，则是音乐文件

                if (cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC)) == 1) {

                    // cursor.getColumnIndex:得到指定列名的索引号,就是说这个字段是第几列
                    // cursor.getString(columnIndex) 可以得到当前行的第几列的值

                    Media media = new Media();// 实例化媒体对象
                    media.setId(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));// 设置歌曲编号
                    media.setName(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));// 设置得到歌曲标题
                    media.setDuration(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));// 设置得到歌曲时长
                    media.setSinger(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));// 设置得到艺术家
                    media.setUri(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));// 设置得到歌曲路径
                    media.setAlbum_id(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)));
                    medias_search.add(media);

                }
            }

            listViewAdapter = new MySouListViewAdapter(SearchActivity.this, medias_search);
            lv_search.setAdapter(listViewAdapter);
        }
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent broadcast = new Intent();
        broadcast.setAction(MyConstant.ACTION_LIST_SEARCH);
        broadcast.putExtra("id", medias_search.get(position).getId());
        sendBroadcast(broadcast);
        finish();
    }
}
