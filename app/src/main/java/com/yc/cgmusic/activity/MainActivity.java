package com.yc.cgmusic.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.ListView;

import com.yc.cgmusic.R;
import com.yc.cgmusic.adapter.MusicListViewAdapter;
import com.yc.cgmusic.adapter.MyViewPagerAdapter;
import com.yc.cgmusic.model.Media;
import com.yc.cgmusic.util.LoadMusicFromSD;
import com.yc.cgmusic.util.PinyinComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener {

    private List<View> views;
    private List<Media> medias;
    private Media media;
    /**
     * view1中的音乐列表
     */
    private ListView listMusic;

    private ViewPager viewPager;
    private View view1,view2;

    private LayoutInflater layoutInflater;

    private PinyinComparator pinyinComparator;

    private MusicListViewAdapter musicListViewAdapter;
    private LoadMusicFromSD loadMusicFromSD;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cg_music_main);

        init();

    }

    /**
     * 初始化控件
     */
    private void init(){
        viewPager = (ViewPager) findViewById(R.id.vp);
        views = new ArrayList<View>();
        medias = new ArrayList<Media>();

        layoutInflater = LayoutInflater.from(this);

        //从SD卡中获取音乐文件
        loadMusicFromSD = new LoadMusicFromSD();
        medias = loadMusicFromSD.loadMediaList(this);

        //创建拼音比较器，用于对medias列表进行排序
        pinyinComparator = new PinyinComparator();
        addView();
        MyViewPagerAdapter mvp = new MyViewPagerAdapter(views);
        viewPager.setAdapter(mvp);


    }
    private void addView(){
        //添加音乐列表View
        view1 = layoutInflater.inflate(R.layout.list_music, null);

        listMusic = (ListView)view1.findViewById(R.id.list_music);
        //对medias进行排序
        Collections.sort(medias,pinyinComparator);
        musicListViewAdapter = new MusicListViewAdapter(this,R.layout.list_music_item,medias);
        listMusic.setAdapter(musicListViewAdapter);

        views.add(view1);

        //添加播放音乐View
        view2 = layoutInflater.inflate(R.layout.cg_play, null);
        views.add(view2);


    }

    @Override
    public void onClick(View v) {


    }
}
