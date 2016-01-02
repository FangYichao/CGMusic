package com.yc.cgmusic.model;

/**
 * Created by Administrator on 2016/1/1.
 */
public class MyConstant {
    public static final String ACTION_PLAY = "com.yc.cgmusic.play"; // 播放
    public static final String ACTION_PAUSE = "com.yc.cgmusic.pause";// 暂停
    public static final String ACTION_LAST = "com.yc.cgmusic.last";// 上一个
    public static final String ACTION_NEXT = "com.yc.cgmusic.next";// 下一个
    public static final String ACTION_LIST = "com.yc.cgmusic.list";// 单击listView条目
    public static final String ACTION_LIST_SEARCH = "com.yc.cgmusic.list_search";// 单机搜索界面的listView条目

    public static final String ACTION_PlAYING_STATE = "com.yc.cgmusic.playing";// 服务发给activity的播放意图
    public static final String ACTION_SERVICR_PUASE = "com.yc.cgmusic.service.puase";// 服务发给activity的暂停意图
    public static final String ACTION_MUSIC_PLAN = "com.yc.cgmusic.music.plan";// 用来发送歌曲当前播放位置的意图
    public static final String ACTION_PLAN_CURRENT = "com.yc.cgmusic.plan_current";// 用来发送进度条位置给服务的意图

    public static int viewPage = 0;
}
