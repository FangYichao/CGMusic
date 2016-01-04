package com.yc.cgmusic.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.yc.cgmusic.model.MyConstant;
import com.yc.cgmusic.service.MusicService;

/**
 * Created by Administrator on 2016/1/4.
 */
public class TrackNextReceiver extends BroadcastReceiver {
    private int position;
    @Override
    public void onReceive(Context context, Intent intent) {
        if (MusicService.position == MusicService.medias.size() - 1) {
            MusicService.position = 0;
            position = MusicService.position;
        } else {
            position = MusicService.position++;
        }

        Intent newIntent = new Intent();
        newIntent.setAction(MyConstant.ACTION_NEXT);
        newIntent.getIntExtra("index", position);
        context.sendBroadcast(newIntent);
    }
}
