package com.jieli.healthaide.ui.sports.ui.set;

public abstract class SportPermission {
    String permissionTitle;
    String permissionDescribe;
    String permissionOperate;

    public SportPermission() {
        init();
    }

    abstract void init();

    abstract void operate();
}
