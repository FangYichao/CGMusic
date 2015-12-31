package com.yc.cgmusic.util;

import com.yc.cgmusic.model.Media;

import java.util.Comparator;

/**
 * Created by Administrator on 2015/12/31.
 */
public class PinyinComparator implements Comparator<Media> {


    @Override
    public int compare(Media lhs, Media rhs) {
        return lhs.getKey().compareTo(rhs.getKey());
    }
}
