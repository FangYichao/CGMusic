package com.yc.cgmusic.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.RemoteViews;

import com.yc.cgmusic.R;
import com.yc.cgmusic.model.Media;
import com.yc.cgmusic.model.MyConstant;
import com.yc.cgmusic.receiver.TrackLastReceiver;
import com.yc.cgmusic.receiver.TrackNextReceiver;
import com.yc.cgmusic.receiver.TrackPlayReceiver;
import com.yc.cgmusic.util.LogUtil;
import com.yc.cgmusic.util.PinyinComparator;

import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2016/1/1.
 */
public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    /**
     * isFirst_play:默认true 如果是第一次就会随机播放一首歌曲，否则就执行暂停或者播放逻辑
     * restart:判断当电话响铃之前是否在播放音乐的标志，如果true，当电话挂断后执行继续播放，否则不播放
     */
    private boolean isFirst_play = true, restart = false;
    private boolean isPlay_No = true;
    /**
     * listener 电话监听对象
     */
    private MyPhoneStateListener listener;

    /**
     * tm 电话管理器对象
     */
    private TelephonyManager telephonyManager;
    /**
     * 媒体播放对象
     */
    private MediaPlayer mediaPlayer;
    /**
     * 音乐文件的集合
     */
    public static List<Media> medias;
    /**
     * 当前音乐在资源集合中的下标
     */
    public static int position;

    /**
     * 创建广播过滤器
     */
    private IntentFilter intentFilter;
    private MyBroadcastReceiver myBroadcastReceiver;

    private NotificationManager notificationManager;
    private Notification notification;
    private RemoteViews remoteViews;
    private Intent playIntent, lastIntent, nextIntent;
    private PendingIntent playPendingIntent, lastPendingIntent, nextPendingIntent;


    private int NOTI_ID = 1;

    @Override
    public void onCreate() {
        super.onCreate();

        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        listener = new MyPhoneStateListener();
        //获取电话监听状态
        telephonyManager.listen(listener, MyPhoneStateListener.LISTEN_CALL_STATE);

        intentFilter = new IntentFilter();
        intentFilter.addAction(MyConstant.ACTION_PLAY);// 播放
        intentFilter.addAction(MyConstant.ACTION_PAUSE);// 暂停
        intentFilter.addAction(MyConstant.ACTION_NEXT);// 下一首
        intentFilter.addAction(MyConstant.ACTION_LAST);// 上一首
        intentFilter.addAction(MyConstant.ACTION_LIST);// 点击listView
        intentFilter.addAction(MyConstant.ACTION_LIST_SEARCH);// 单击搜索界面的listView条目
        intentFilter.addAction(MyConstant.ACTION_PLAN_CURRENT);// 用来发送进度条位置给服务的意图

        myBroadcastReceiver = new MyBroadcastReceiver();//创建一个广播接收者，注册并过滤
        registerReceiver(myBroadcastReceiver, intentFilter);

    }

    /**
     * 服务被启动
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mediaPlayer = new MediaPlayer();
        medias = intent.getParcelableArrayListExtra("medias");
        initNotification();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        isPlay_No = false;
        mediaPlayer.release();
        mediaPlayer = null;
        telephonyManager.listen(listener, MyPhoneStateListener.LISTEN_NONE);
        cancelNoti();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 自定义的电话监听器 用来监听电话状态，并作出相应动作
     */
    private class MyPhoneStateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    // 电话闲置或者挂断时
                    if (restart) {
                        mediaPlayer.start();
                        updateNotification();
                        sendPlayingWord(position);
                    }
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    // 电话接通后
                    updateNotification();
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    // 电话响铃时
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                        updateNotification();
                        sendPause();
                        restart = true;
                    }
                    break;
            }


        }
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (!(medias.size() == 0 || medias == null)) {
                if (MyConstant.ACTION_PLAY.equals(intent.getAction())) {
                    //如果播放器没有在播放音乐
                    if (!mediaPlayer.isPlaying()) {
                        if (isFirst_play) {
                            position = intent.getIntExtra("index", 0);
                            prepareMusic(position);
                        } else {
                            mediaPlayer.start();
                            sendCurrentPosition();
                            updateNotification();
                            sendPlayingWord(position);
                        }
                    } else {
                        mediaPlayer.pause();
                        updateNotification();
                        sendPause();

                    }
                } else if (MyConstant.ACTION_PAUSE.equals(intent.getAction())) {
                    //暂停
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                        updateNotification();
                        sendPause();
                    }
                } else if (MyConstant.ACTION_NEXT.equals(intent.getAction())) {
                    //下一首
                    //如果是第一次播放音乐
                    if (isFirst_play) {
                        position = intent.getIntExtra("index", 0);
                        updateNotification();
                        prepareMusic(position);

                    } else {
                        position = intent.getIntExtra("index", position);
                        updateNotification();
                        prepareMusic(position);
                    }
                    LogUtil.e("下一首当前播放", String.valueOf(position));

                } else if (MyConstant.ACTION_LAST.equals(intent.getAction())) {
                    //上一首
                    if (isFirst_play) {
                        position = intent.getIntExtra("index", 0);
                        updateNotification();
                        prepareMusic(position);
                    } else {
                        position = intent.getIntExtra("index", position);
                        updateNotification();
                        prepareMusic(position);
                    }
                    LogUtil.e("上一首当前播放", String.valueOf(position));
                    LogUtil.e("音乐数目", String.valueOf(medias.size()));
                } else if (MyConstant.ACTION_LIST.equals(intent.getAction())) {
                    //单击音乐条目
                    if (isFirst_play) {
                        position = intent.getIntExtra("index", 0);
                        updateNotification();
                        prepareMusic(position);

                    } else {
                        position = intent.getIntExtra("index", position);
                        updateNotification();
                        prepareMusic(position);
                    }
                    LogUtil.e("当前播放", String.valueOf(position));
                } else if (MyConstant.ACTION_LIST_SEARCH.equals(intent.getAction())) {
                    //单击搜索界面的音乐条目
                    String id = intent.getStringExtra("id");
                    for (int i = 0; i < medias.size(); i++) {
                        if (id.equals(medias.get(i).getId())) {
                            position = i;
                            prepareMusic(position);
                            break;
                        }
                    }

                } else if (MyConstant.ACTION_PLAN_CURRENT.equals(intent.getAction())) {
                    //拖动进度条
                    mediaPlayer.seekTo(intent.getIntExtra("index", 0));

                }
                LogUtil.i("Action:", intent.getAction());
            }
        }
    }

    /**
     * 播放音乐之前的准备
     *
     * @param index 要准备的音乐在资源集合里的下标
     */
    private void prepareMusic(int index) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        mediaPlayer.reset();
        String musicUri = medias.get(index).getUri();
        Uri songUri = Uri.parse(musicUri);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(getApplicationContext(), songUri);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.prepareAsync();
        updateNotification();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mediaPlayer.start();
        isFirst_play = false;
        sendCurrentPosition();
        sendPlayingWord(position);
        updateNotification();

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mp != null) {
            if (position == medias.size() - 1) {
                position = 0;
            } else {
                position = position + 1;
            }
            prepareMusic(position);
        }

    }

    /***
     * 每当音乐开始播放时，向activity发送广播更新界面相关信息
     *
     * @param position 当前歌曲的位置
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
        updateNotification();
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

    /**
     * 自定义通知栏
     */
    public void initNotification() {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notification = new Notification();
        notification.icon = R.mipmap.logo;
        notification.tickerText = "欢迎使用超歌播放器";
        remoteViews = new RemoteViews(getPackageName(), R.layout.layout_notification);
        remoteViews.setImageViewResource(R.id.iv_art_noti, R.mipmap.fengmian);
        if (medias.size() == 0 || medias == null) {
            remoteViews.setTextViewText(R.id.noti_title, "欢迎使用");
            remoteViews.setTextViewText(R.id.noti_small_title, "超歌音乐播放器");
        } else {
            remoteViews.setTextViewText(R.id.noti_title, medias.get(position).getName());
            remoteViews.setTextViewText(R.id.noti_small_title, medias.get(position).getSinger());
        }
        notification.contentView = remoteViews;
        if (lastIntent == null) {
            lastIntent = new Intent(this, TrackLastReceiver.class);
        }
        if (playIntent == null) {
            playIntent = new Intent(this, TrackPlayReceiver.class);
        }
        if (nextIntent == null) {
            nextIntent = new Intent(this, TrackNextReceiver.class);
        }
        if (lastPendingIntent == null) {
            lastPendingIntent = PendingIntent.getBroadcast(this, 0, lastIntent, 0);
        }
        if (playPendingIntent == null) {
            playPendingIntent = PendingIntent.getBroadcast(this, 0, playIntent, 0);
        }
        if (nextPendingIntent == null) {
            nextPendingIntent = PendingIntent.getBroadcast(this, 0, nextIntent, 0);
        }

        //自定义通知栏的单击事件
        remoteViews.setOnClickPendingIntent(R.id.btn_noti_last, lastPendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.btn_noti_pause, playPendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.btn_noti_next, nextPendingIntent);

        // 点击跳转进应用程序
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(new ComponentName(getPackageName(), "com.yc.cgmusic.MainActivity"));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 100, intent, 0);

        // 点击的事件
        notification.contentIntent = contentIntent;

        // 点击通知之后不消失
        notification.flags = Notification.FLAG_NO_CLEAR;
        notificationManager.notify(NOTI_ID, notification);
    }

    /**
     * 更新通知栏界面
     */
    public void updateNotification() {
        remoteViews.setTextViewText(R.id.noti_title, medias.get(position).getName());
        remoteViews.setTextViewText(R.id.noti_small_title, medias.get(position).getSinger());

        if (mediaPlayer.isPlaying()) {
            remoteViews.setImageViewResource(R.id.btn_noti_pause, R.mipmap.notification_pause);

        } else {

            remoteViews.setImageViewResource(R.id.btn_noti_pause, R.mipmap.notification_play);
        }	// 获取专辑
        int Album = medias.get(position).getAlbum_id();
        String img = getAlbumArt(Album);
        Bitmap bm = null;
        System.out.println("Album:" + img);
        if (img != null) {
            bm = BitmapFactory.decodeFile(img);
            if (bm != null) {
                // 设置图片格式
                BitmapDrawable bmpDraw = new BitmapDrawable(bm);
                LogUtil.v("TAG", "bmpDraw有没有:" + bmpDraw);
                // 设置专辑图片
                remoteViews.setImageViewBitmap(R.id.iv_art_noti, bm);
            } else {
                remoteViews.setImageViewResource(R.id.iv_art_noti, R.mipmap.fengmian);
            }
        } else {
            remoteViews.setImageViewResource(R.id.iv_art_noti, R.mipmap.fengmian);
        }
        notificationManager.notify(NOTI_ID, notification);
    }

    public void cancelNoti() {
        notificationManager.cancel(NOTI_ID);
    }	/**********************************************************************
     *
     * 拿到专辑图片的路径
     */
    private String getAlbumArt(int album_id) {
        String mUriAlbums = "content://media/external/audio/albums";
        String[] projection = new String[] { "album_art" };
        Cursor cur = this.getContentResolver().query(Uri.parse(mUriAlbums + "/" + Integer.toString(album_id)),
                projection, null, null, null);
        String album_art = null;
        if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
            cur.moveToNext();
            album_art = cur.getString(0);
        }
        cur.close();
        cur = null;
        return album_art;
    }

}
