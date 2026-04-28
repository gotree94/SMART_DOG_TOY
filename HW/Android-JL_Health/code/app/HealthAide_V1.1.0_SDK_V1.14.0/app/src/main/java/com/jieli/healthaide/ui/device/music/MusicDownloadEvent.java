package com.jieli.healthaide.ui.device.music;
/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/12/21 5:23 PM
 * @desc :
 */
public class MusicDownloadEvent {
    public static final int TYPE_DOWNLOAD = 1;
    public static final int TYPE_FINISH = 2;
    public static final int TYPE_CANCEL = 3;
    public static final int TYPE_ERROR = 4;


    private int index;
    private int total;
    private int percent;

    private String name;
    private int type;


    public MusicDownloadEvent(int type) {
        this.type = type;
    }

    public MusicDownloadEvent(int type, int index, int total, int percent, String name) {

        this.index = index;
        this.total = total;
        this.percent = percent;
        this.name = name;
        this.type = type;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}