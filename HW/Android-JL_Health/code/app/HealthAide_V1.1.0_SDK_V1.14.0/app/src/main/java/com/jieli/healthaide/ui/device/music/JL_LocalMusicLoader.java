package com.jieli.healthaide.ui.device.music;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/12/21 5:23 PM
 * @desc :
 */
public class JL_LocalMusicLoader {
    private String tag = getClass().getSimpleName();
    /**
     * @param condition 搜素条件
     * @return
     */
    private ContentResolver mContentResolver;
    private final static Uri contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    private List<Music> localMusic;
    private MyContentObserver myContentObserver;
    private List<MusicObserver> observers = new ArrayList<>();


    public JL_LocalMusicLoader(ContentResolver mContentResolver) {
        this.mContentResolver = mContentResolver;
        myContentObserver = new MyContentObserver(new Handler(Looper.getMainLooper()));
        mContentResolver.registerContentObserver(contentUri, false, myContentObserver);
    }

    private static String[] projection = new String[]
            {
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.SIZE,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.IS_MUSIC,
                    MediaStore.Audio.Media.DISPLAY_NAME,
            };


    public List<Music> loadAll() {
        if (localMusic == null) {
            localMusic = load("");
        }
        return localMusic;
    }


    public List<Music> load(String condition) {
        if (condition == null) {
            condition = "";
        }
        List<Music> mAllMusics = new ArrayList<>();
        Cursor cursor = mContentResolver.query(contentUri, projection, MediaStore.Audio.Media.TITLE + " LIKE " + "'%" + condition + "%'", null, "_id");


        if (cursor == null) {
            return mAllMusics;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return mAllMusics;
        }

        do {
            String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
            Music music = new Music(
                    cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID)),
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)),
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)),
                    cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)),
                    cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)),
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)),
                    url,
                    null,
                    0
            );
            if (cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC)) != 0 && music.getDuration() >= 10000) {
                mAllMusics.add(music);
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                    String uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build().toString();
                    music.setUri(uri);
                }
            }
        } while (cursor.moveToNext());

        cursor.close();
        return mAllMusics;
    }


    public void registerMusicObserver(MusicObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void unregisterMusicObserver(MusicObserver observer) {
        observers.remove(observer);
    }

    public void clean() {
        localMusic.clear();
        localMusic = null;
    }

    public void release() {
        clean();
        mContentResolver.unregisterContentObserver(myContentObserver);
    }

    public interface MusicObserver {
        void onChange(List<Music> list);
    }

    private class MyContentObserver extends ContentObserver {
        private Handler handler;
        private Runnable changeTask = () -> handlerChange();

        public MyContentObserver(Handler handler) {
            super(handler);
            this.handler = handler;
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (localMusic == null) {
                return;
            }
            handler.removeCallbacks(changeTask);
            handler.postDelayed(changeTask, 1000);
        }
    }


    private void handlerChange() {
        localMusic = null;
        loadAll();
        for (MusicObserver observer : observers) {
            observer.onChange(localMusic);
        }
    }

}
