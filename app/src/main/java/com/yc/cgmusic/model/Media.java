package com.yc.cgmusic.model;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * 媒体实体类
 */
public class Media implements Parcelable {
    private String name;
    private String uri;
    private String singer;
    private String id;
    private int duration;
    private int album_id;
    private String key;

    public Media() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getAlbum_id() {
        return album_id;
    }

    public void setAlbum_id(int album_id) {
        this.album_id = album_id;
    }



    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return "Media [name=" + name + ", id=" + id + ", uri=" + uri + ", duration=" + duration + ", singer=" + singer
                + ", album_id=" + album_id + ", key=" + key + "]";
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(uri);
        dest.writeString(singer);
        dest.writeString(id);
        dest.writeInt(duration);
        dest.writeInt(album_id);
        dest.writeString(key);
    }
    public static final Parcelable.Creator<Media> CREATOR = new Creator<Media>() {
        @Override
        public Media[] newArray(int size) {
            return new Media[size];
        }

        @Override
        public Media createFromParcel(Parcel in) {
            return new Media(in);
        }
    };
    public Media(Parcel in) {
        name = in.readString();
        uri = in.readString();
        singer = in.readString();
        id = in.readString();
        duration = in.readInt();
        album_id = in.readInt();
        key = in.readString();
    }

}
