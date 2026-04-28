package com.jieli.watchtesttool.ui.file.model;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/16/21 8:40 AM
 * @desc :
 */
public class MusicPlayInfo {
     private MusicNameInfo musicNameInfo;
     private MusicStatusInfo musicStatusInfo;
     private PlayModeInfo playModeInfo;

     private int deviceMode;


    public MusicNameInfo getMusicNameInfo() {
        return musicNameInfo;
    }

    public void setMusicNameInfo(MusicNameInfo musicNameInfo) {
        this.musicNameInfo = musicNameInfo;
    }

    public MusicStatusInfo getMusicStatusInfo() {
        return musicStatusInfo;
    }

    public void setMusicStatusInfo(MusicStatusInfo musicStatusInfo) {
        this.musicStatusInfo = musicStatusInfo;
    }

    public PlayModeInfo getPlayModeInfo() {
        return playModeInfo;
    }

    public void setPlayModeInfo(PlayModeInfo playModeInfo) {
        this.playModeInfo = playModeInfo;
    }

    public int getDeviceMode() {
        return deviceMode;
    }

    public void setDeviceMode(int deviceMode) {
        this.deviceMode = deviceMode;
    }
}
