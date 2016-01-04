package com.yc.cgmusic.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.yc.cgmusic.model.MyConstant;

/**
 * Created by Administrator on 2016/1/4.
 */
public class TrackPlayReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent newIntent = new Intent();
        newIntent.setAction(MyConstant.ACTION_PLAY);
        context.sendBroadcast(newIntent);
    }
}
