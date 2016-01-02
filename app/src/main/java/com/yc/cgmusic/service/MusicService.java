package com.yc.cgmusic.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.yc.cgmusic.model.Media;
import com.yc.cgmusic.model.MyConstant;
import com.yc.cgmusic.util.LogUtil;
import com.yc.cgmusic.util.PinyinComparator;

import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2016/1/1.
 */
public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    private boolean isFirst_play = true;
    private boolean isPlay_No =true;
    /**
     * 媒体播放对象
     */
    private MediaPlayer mediaPlayer;
    /**
     * 音乐文件的集合
     */
    private List<Media> medias;
    /**
     * 当前音乐在资源集合中的下标
     */
    private int position;

    /**
     * 创建广播过滤器
     */
    private IntentFilter intentFilter;
    private MyBroadcastReceiver myBroadcastReceiver;

    @Override
    public void onCreate() {
        super.onCreate();

        intentFilter = new IntentFilter();
        intentFilter.addAction(MyConstant.ACTION_PLAY);// 播放
        intentFilter.addAction(MyConstant.ACTION_PAUSE);// 暂停
        intentFilter.addAction(MyConstant.ACTION_NEXT);// 下一首
        intentFilter.addAction(MyConstant.ACTION_LAST);// 上一首
        intentFilter.addAction(MyConstant.ACTION_LIST);// 点击listView
        intentFilter.addAction(MyConstant.ACTION_LIST_SEARCH);// 单击搜索界面的listView条目
        intentFilter.addAction(MyConstant.ACTION_PLAN_CURRENT);// 用来发送进度条位置给服务的意图

        myBroadcastReceiver = new MyBroadcastReceiver();//创建一个广播接收者，注册并过滤
        registerReceiver(myBroadcastReceiver,intentFilter);

    }

    /**
     * 服务被启动
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mediaPlayer = new MediaPlayer();
        medias = intent.getParcelableArrayListExtra("medias");

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {

        mediaPlayer.release();
        mediaPlayer = null;
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public class MyBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            PinyinComparator pinyinComparator = new PinyinComparator();
            Collections.sort(medias, pinyinComparator);
            if (MyConstant.ACTION_PLAY.equals(intent.getAction())){
                //如果播放器没有在播放音乐
                if (!mediaPlayer.isPlaying()){
                    if (isFirst_play) {
                        position = intent.getIntExtra("index", 0);
                        prepareMusic(position);
                    }else {
                        mediaPlayer.start();
                        sendCurrentPosition();
                        sendPlayingWord(position);
                    }
                }else {
                    mediaPlayer.pause();
                    sendPause();
                }
            }else if (MyConstant.ACTION_PAUSE.equals(intent.getAction())){
                //暂停
                if (mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                    sendPause();
                }
            }else if (MyConstant.ACTION_NEXT.equals(intent.getAction())){
                //下一首
                //如果是第一次播放音乐
                if (isFirst_play){
                    position = intent.getIntExtra("index",0);
                    prepareMusic(position);

                }else {
                    position = intent.getIntExtra("index",position);
                    prepareMusic(position);
                }
                LogUtil.e("下一首当前播放", String.valueOf(position));

            }else if (MyConstant.ACTION_LAST.equals(intent.getAction())){
                //上一首
                if (isFirst_play){
                    position = intent.getIntExtra("index",0);
                    prepareMusic(position);
                }else {
                    position = intent.getIntExtra("index",position);
                    prepareMusic(position);
                }
                LogUtil.e("上一首当前播放", String.valueOf(position));
                LogUtil.e("音乐数目", String.valueOf(medias.size()));
            }else if (MyConstant.ACTION_LIST.equals(intent.getAction())){
                //单击音乐条目
                if (isFirst_play){
                    position = intent.getIntExtra("index",0);
                    prepareMusic(position);

                }else {
                    position = intent.getIntExtra("index",position);
                    prepareMusic(position);
                }
                LogUtil.e("当前播放", String.valueOf(position));
            }else if (MyConstant.ACTION_LIST_SEARCH.equals(intent.getAction())){
                //单击搜索界面的音乐条目

            }else if (MyConstant.ACTION_PLAN_CURRENT.equals(intent.getAction())){
                //拖动进度条
                mediaPlayer.seekTo(intent.getIntExtra("index",0));

            }
            LogUtil.i("Action:",intent.getAction());

        }
    }
    /**
     * 播放音乐之前的准备
     * @param index 要准备的音乐在资源集合里的下标
     */
    private void prepareMusic(int index){
        if (mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
        mediaPlayer.reset();
        String musicUri = medias.get(index).getUri();
        Uri songUri = Uri.parse(musicUri);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(getApplicationContext(),songUri);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.prepareAsync();
    }
    @Override
    public void onPrepared(MediaPlayer mp) {
        mediaPlayer.start();
        isFirst_play = false;
        sendCurrentPosition();
        sendPlayingWord(position);

    }
    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mp!=null){
            if (position == medias.size()-1){
                position =0;
            }else {
                position = position+1;
            }
            prepareMusic(position);
        }

    }

    /***
     * 每当音乐开始播放时，向activity发送广播更新界面相关信息
     * @param position  当前歌曲的位置
     */
    public void sendPlayingWord(int position) {
        Intent intent = new Intent();
        intent.setAction(MyConstant.ACTION_PlAYING_STATE);
        intent.putExtra("media", position);
        sendBroadcast(intent);
    }
    /**
     * 发送广播给activity歌曲已经暂停
     */
    public void sendPause() {
        Intent intent = new Intent();
        intent.setAction(MyConstant.ACTION_SERVICR_PUASE);
        sendBroadcast(intent);
    }

    /**
     * 发送歌曲进度到activity
     */
    public void sendCurrentPosition() {
        new Thread() {
            public void run() {
                Intent intent = new Intent();
                while (isPlay_No) {
                    int playerPosition = mediaPlayer.getCurrentPosition();
                    intent.setAction(MyConstant.ACTION_MUSIC_PLAN);
                    intent.putExtra("playerPosition", playerPosition);
                    sendBroadcast(intent);
                    LogUtil.i("jindu", "发到界面的进度条值:" + playerPosition);
                    try {
                        sleep(500);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();


    }


}
