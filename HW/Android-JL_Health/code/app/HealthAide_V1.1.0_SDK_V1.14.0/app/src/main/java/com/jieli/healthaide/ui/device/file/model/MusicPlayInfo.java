package com.jieli.healthaide.ui.device.file.model;

import androidx.annotation.NonNull;

import com.jieli.jl_rcsp.model.device.MusicNameInfo;
import com.jieli.jl_rcsp.model.device.MusicStatusInfo;
import com.jieli.jl_rcsp.model.device.PlayModeInfo;

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

    @NonNull
    @Override
    public String toString() {
        return "MusicPlayInfo{" +
                "musicNameInfo=" + musicNameInfo +
                ", musicStatusInfo=" + musicStatusInfo +
                ", playModeInfo=" + playModeInfo +
                ", deviceMode=" + deviceMode +
                '}';
    }
}
