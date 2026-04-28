package com.jieli.healthaide.ui.device.music;

import androidx.annotation.Keep;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/12/21 5:23 PM
 * @desc :
 */
@Keep
public class Music {
    private long id;
    private String title;
    private String album;
    private int duration;
    private long size;
    private String artist;
    private String url;
    private String coverUrl;
    private boolean selected;
    private boolean collect;
    private int local; //0:本地 ,1:网络 ,5:图灵H5类型,2.短音频 3:m3u8 4:直播的m3u8
    private int download;   //    1：未下载，2：已下载,3：正在下载

    private String auth;
    private int position = 0;
    private String uri;

    boolean isHistory = false; //sdk内部使用
    boolean isM3u8 = false; //sdk内部使用

    public void setDownload(int download) {
        this.download = download;
    }

    public int getDownload() {
        return download;
    }

    public void setLocal(int local) {
        this.local = local;
    }

    public int getLocal() {
        return local;
    }





    public Music(long id, String title, String album, int duration, long size, String artist, String url, String coverUrl, int local) {
        this.id = id;
        this.title = title;
        this.album = album;
        this.duration = duration;
        this.size = size;
        this.artist = artist;
        this.url = url;
        this.coverUrl = coverUrl;
        this.local = local;
    }

    public Music(long id, String title, String artist, String url, String coverUrl, int local) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.url = url;
        this.coverUrl = coverUrl;
        this.local = local;
    }

    public Music() {
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getUri() {
        return uri;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isCollect() {
        return collect;
    }

    public void setCollect(boolean collect) {
        this.collect = collect;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public String getAuth() {
        return auth;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return "Music{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", album='" + album + '\'' +
                ", duration=" + duration +
                ", size=" + size +
                ", artist='" + artist + '\'' +
                ", url='" + url + '\'' +
                ", coverUrl='" + coverUrl + '\'' +
                ", selected=" + selected +
                ", collect=" + collect +
                ", download=" + download +
                ", local=" + local +
                ", auth=" + auth +
                ", position=" + position +
                '}';
    }
}
