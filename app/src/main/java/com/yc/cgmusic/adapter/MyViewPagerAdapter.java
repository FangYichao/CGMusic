package com.yc.cgmusic.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by Administrator on 2015/12/30.
 */
public class MyViewPagerAdapter extends PagerAdapter {
    private List<View> views;

    public MyViewPagerAdapter(List<View> views) {
        this.views = views;
    }


    @Override
    public int getCount() {
        return views == null ? 0 : views.size();
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = views.get(position);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        View view = views.get(position);
        container.removeView(view);
    }
}
