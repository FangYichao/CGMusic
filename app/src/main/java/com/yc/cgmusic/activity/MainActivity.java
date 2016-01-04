package com.yc.cgmusic.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yc.cgmusic.R;
import com.yc.cgmusic.adapter.MusicListViewAdapter;
import com.yc.cgmusic.adapter.MyViewPagerAdapter;
import com.yc.cgmusic.model.Media;
import com.yc.cgmusic.model.MyConstant;
import com.yc.cgmusic.service.MusicService;
import com.yc.cgmusic.util.LoadMusicFromSD;
import com.yc.cgmusic.util.LogUtil;
import com.yc.cgmusic.util.PinyinComparator;
import com.yc.cgmusic.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener,View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    /**
     * 用于记录当前显示的按钮是播放按钮还是暂停按钮，true表示播放按钮，也就是目前处于暂停播放状态
     */
    private boolean flag = true;

    private List<View> views;
    private List<Media> medias;
    private Media media;

    /**
     * view1中的音乐列表
     */
    private ListView listMusic;
    private ImageView lastButton;
    private ImageView playButton;
    private ImageView nextButton;
    private ImageView sou;
    private TextView playTitle;
    private TextView playSinger;
    private TextView musicCurrentTime;
    private TextView musicAlwaysTime;
    private SeekBar sbMusic;

    private ViewPager viewPager;
    private View view1,view2;

    private LayoutInflater layoutInflater;
    private IntentFilter intentFilter;
    private PinyinComparator pinyinComparator;

    private MusicListViewAdapter musicListViewAdapter;
    private LoadMusicFromSD loadMusicFromSD;

    private MainBroadcastReceiver mainBroadcastReceiver;


    private int currentPosition;
    /**
     * 第一次按back时间；
     */
    private long time = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cg_music_main);

        init();

        sou.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSearchRequested();
            }
        });

    }

    /**
     * 初始化控件
     */
    private void init(){
        lastButton = (ImageView)findViewById(R.id.last_button);
        playButton = (ImageView)findViewById(R.id.play_button);
        nextButton = (ImageView)findViewById(R.id.next_button);
        sou = (ImageView)findViewById(R.id.music_search);

        lastButton.setOnClickListener(this);
        playButton.setOnClickListener(this);
        nextButton.setOnClickListener(this);

        viewPager = (ViewPager) findViewById(R.id.vp);
        views = new ArrayList<View>();
        medias = new ArrayList<Media>();

        //构造一个布局填充器
        layoutInflater = LayoutInflater.from(this);

        //从SD卡中获取音乐文件
        loadMusicFromSD = new LoadMusicFromSD();
        medias = loadMusicFromSD.loadMediaList(this);

        //创建拼音比较器，用于对medias列表进行排序
        pinyinComparator = new PinyinComparator();
        addView();
        //设置viewPager的适配器
        MyViewPagerAdapter mvp = new MyViewPagerAdapter(views);
        viewPager.setAdapter(mvp);

        //创建广播过滤器
        mainBroadcastReceiver = new MainBroadcastReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction(MyConstant.ACTION_PlAYING_STATE);
        intentFilter.addAction(MyConstant.ACTION_SERVICR_PUASE);
        intentFilter.addAction(MyConstant.ACTION_MUSIC_PLAN);
        intentFilter.addAction(MyConstant.ACTION_PLAY);
        registerReceiver(mainBroadcastReceiver,intentFilter);



        Intent service = new Intent(this, MusicService.class);
        service.putParcelableArrayListExtra("medias", (ArrayList<? extends Parcelable>) medias);
        startService(service);
    }
    private void addView(){
        //添加音乐列表View
        view1 = layoutInflater.inflate(R.layout.list_music, null);

        listMusic = (ListView)view1.findViewById(R.id.list_music);
        //对medias进行排序
        Collections.sort(medias,pinyinComparator);
        musicListViewAdapter = new MusicListViewAdapter(this,R.layout.list_music_item,medias);
        listMusic.setAdapter(musicListViewAdapter);
        listMusic.setOnItemClickListener(this);
        views.add(view1);


        //添加播放音乐View
        view2 = layoutInflater.inflate(R.layout.cg_play, null);

        playTitle = (TextView)view2.findViewById(R.id.play_title);
        playSinger = (TextView)view2.findViewById(R.id.play_singer);
        musicCurrentTime = (TextView)view2.findViewById(R.id.music_current_time);
        musicAlwaysTime = (TextView)view2.findViewById(R.id.music_always_time);
        sbMusic = (SeekBar)view2.findViewById(R.id.sb_music);
        sbMusic.setOnSeekBarChangeListener(this);

        views.add(view2);

        setPlayText(0);
    }

    /**
     * 向广播接收器发送当前音乐条目以及意图
     * @param position 当前操作歌曲的下标
     * @param action 发送给广播接收器的action类型
     */
    private void music_play(int position,String action){
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra("index", position);
        sendBroadcast(intent);
    }



    /**
     * 设置音乐播放界面的歌名以及歌手
     * @param index 传入的歌曲在medias中的下标
     */
    private void setPlayText(int index){
        if (medias.size() == 0||medias ==null){
            playTitle.setText("超歌播放器");
            playSinger.setText("");

        }else {
            playTitle.setText(medias.get(index).getName());
            playSinger.setText(medias.get(index).getSinger());
        }
    }

    /**
     * 音乐ListView的点击事件，点击将当前条目下标以及操作类型封装到intent里发出一条广播，由MusicService中的广播接收
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        currentPosition = position;
        LogUtil.e("11111111111111111", String.valueOf(position));
        music_play(currentPosition, MyConstant.ACTION_LIST);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.last_button:
                if (Utils.isFastClick()) {
                    return ;
                }
                if (medias.size()==0||medias==null){
                    Toast.makeText(this,"当前乐库没有任何音乐",Toast.LENGTH_SHORT).show();
                }else {
                    if (currentPosition == 0) {
                        currentPosition = medias.size()-1;
                    } else {
                        currentPosition = currentPosition-1;
                    }
                    music_play(currentPosition, MyConstant.ACTION_LAST);
                }

                LogUtil.e("上一首",String.valueOf(currentPosition));
                break;

            case R.id.play_button:
                if (medias.size()==0||medias==null){
                    Toast.makeText(this,"当前乐库没有任何音乐",Toast.LENGTH_SHORT).show();
                }else {
                    //发送播放广播
                    if (flag){
                        sendBroadcastToService( MyConstant.ACTION_PLAY,0,null);
                    }else {//发送暂停广播
                        sendBroadcastToService(MyConstant.ACTION_PAUSE, 0, null);
                    }
                    flag = !flag;
                }
                break;

            case R.id.next_button:
                if (Utils.isFastClick()) {
                    return ;
                }
                if (medias.size()==0||medias==null){
                    Toast.makeText(this,"当前乐库没有任何音乐",Toast.LENGTH_SHORT).show();
                }else {
                    if (currentPosition == medias.size()-1) {
                        currentPosition = 0;
                    } else {
                        currentPosition = currentPosition+1;
                    }
                    music_play(currentPosition, MyConstant.ACTION_NEXT);
                }
                LogUtil.e("下一首",String.valueOf(currentPosition));
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    /**
     *手动调节进度条
     * @param seekBar
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int mediaPlayer = seekBar.getProgress();
        sendBroadcastToService(MyConstant.ACTION_PLAN_CURRENT, mediaPlayer, null);
    }
    /**
     * 向服务发送广播
     * @param action 发送的类型
     * @param intExtra 用于通知服务从歌曲哪个进度开始播放
     * @param stringExtra
     */
    private void sendBroadcastToService(String action, int intExtra, String stringExtra){
        Intent intentToSercier = new Intent();
        intentToSercier.setAction(action);
        intentToSercier.putExtra("index", intExtra);
        intentToSercier.putExtra("date", stringExtra);
        sendBroadcast(intentToSercier);
    }
    /**
     * 主要用于接收服务发来的广播更新播放界面
     */
    private class MainBroadcastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if (MyConstant.ACTION_PlAYING_STATE.equals(intent.getAction())){
                //收到服务开始播放的广播
                int index = intent.getIntExtra("media",0);
                currentPosition = index;

                setPlayText(index);
                //收到播放信息，改变播放按钮标志
                playButton.setImageResource(R.mipmap.suspend_button);
                //改变播放界面歌曲信息
                musicAlwaysTime.setText(timeconvert(medias.get(index).getDuration()));
                sbMusic.setMax(medias.get(index).getDuration());

            }else if (MyConstant.ACTION_SERVICR_PUASE.equals(intent.getAction())){
                //收到服务暂停播放的广播
                playButton.setImageResource(R.mipmap.play_button);


            }else if (MyConstant.ACTION_MUSIC_PLAN.equals(intent.getAction())){
                //收到服务发送的播放进度的广播
                int playerPosition = intent.getIntExtra("playerPosition",0);
                String playerTime = timeconvert(playerPosition);
                LogUtil.i("jindu", "服务发来的进度条值:" + playerTime);
                sbMusic.setProgress(playerPosition);
                musicCurrentTime.setText(playerTime);


            }else if (MyConstant.ACTION_PLAY.equals(intent.getAction())){
                //播放开始的广播
                if (flag){
                    playButton.setImageResource(R.mipmap.suspend_button);
                }else {
                    playButton.setImageResource(R.mipmap.play_button);
                }
                flag = !flag;

            }



        }
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
    /**
     * 双击返回桌面方法和菜单监控
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - time > 1000)) {
                Toast.makeText(this, "再按一次返回桌面", Toast.LENGTH_SHORT).show();
                time = System.currentTimeMillis();
            } else {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);

                startActivity(intent);
            }
            return true;
        }
//        else if (keyCode == KeyEvent.KEYCODE_MENU) {
//            new ActionSheetDialog(MainActivity.this).builder().setTitle("菜单").setCancelable(false)
//                    .setCanceledOnTouchOutside(false)
//                    .addSheetItem("退出应用", SheetItemColor.Red, new OnSheetItemClickListener() {
//                        @Override
//                        public void onClick(int which) {
//                            // MusicService.isPlay_No = false;
//                            Intent intent = new Intent(MainActivity.this, MusicService.class);
//                            stopService(intent);
//                            MyApplication.getInstance().killActivity();
//                            MainActivity.this.finish();
//                        }
//                    }).show();
//
//            return true;
//        }
        else {
            return super.onKeyDown(keyCode, event);
        }

    }
}
