package com.jieli.healthaide.ui.sports.viewmodel;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.R;
import com.jieli.healthaide.data.entity.SportRecord;
import com.jieli.healthaide.tool.watch.synctask.SyncTaskManager;
import com.jieli.healthaide.tool.watch.synctask.UploadSportsRecordSyncTask;
import com.jieli.healthaide.ui.sports.listener.SportsInfoListener;
import com.jieli.healthaide.ui.sports.model.BaseRealData;
import com.jieli.healthaide.ui.sports.model.LocationRealData;
import com.jieli.healthaide.ui.sports.model.RequestRecordState;
import com.jieli.healthaide.ui.sports.model.SportsInfo;
import com.jieli.healthaide.ui.sports.record.DeviceRequestRecordHandler;
import com.jieli.healthaide.ui.sports.service.AbstractSportsServerImpl;
import com.jieli.healthaide.ui.sports.service.DeviceIndoorRunningServiceImpl;
import com.jieli.healthaide.ui.sports.service.DeviceOutdoorRunningServiceImpl;
import com.jieli.healthaide.ui.sports.service.SportsService;
import com.jieli.jl_filebrowse.interfaces.OperatCallback;
import com.jieli.jl_rcsp.util.JL_Log;

import java.lang.reflect.InvocationTargetException;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/30/21
 * @desc :
 */
public class SportsViewModel extends AndroidViewModel implements SportsService, SportsInfoListener {
    private static final String TAG = SportsViewModel.class.getSimpleName();

    private final AbstractSportsServerImpl sportsService;
    @SuppressLint("StaticFieldLeak")
    private final Context context;

    private SoundPool soundPool;

    private final MutableLiveData<BaseRealData> baseRealDataMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<SportsInfo> sportsInfoMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<LocationRealData> locationRealDataMutableLiveData = new MutableLiveData<>();

    private final MutableLiveData<RequestRecordState> requestRecordLiveData = new MutableLiveData<>();

    public SportsViewModel(@NonNull Application application, Integer type) {
        super(application);
        this.context = application.getApplicationContext();
        this.sportsService = getSportsServiceByType(type);
        this.sportsService.setSportsInfoListener(this);
    }


    @Override
    protected void onCleared() {
        super.onCleared();
        releaseSoundPool();
    }

    @Override
    public void start() {
        requestRecordLiveData.postValue(RequestRecordState.NO_STATE);
        sportsService.start();
    }

    @Override
    public void pause() {
        sportsService.pause();
    }

    @Override
    public void resume() {
        sportsService.resume();
    }

    @Override
    public void stop() {
        sportsService.stop();
    }


    public <T extends BaseRealData> LiveData<T> getRealDataLiveData() {
        return (LiveData<T>) baseRealDataMutableLiveData;
    }


    public LiveData<SportsInfo> getSportInfoLiveData() {
        return sportsInfoMutableLiveData;
    }


    public LiveData<LocationRealData> getLocationRealDataData() {
        return locationRealDataMutableLiveData;
    }

    private void playSound(int id) {
        if (null == soundPool) {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    soundPool = new SoundPool.Builder()
                            .setMaxStreams(1)
                            .setAudioAttributes(
                                    new AudioAttributes.Builder()
                                            .setLegacyStreamType(AudioManager.STREAM_RING)
                                            .build())
                            .build();
                } else {
                    soundPool = new SoundPool(1, AudioManager.STREAM_RING, 0);
                }
            } catch (Exception ignore) {

            }
        }
        if (null == soundPool) return;
        soundPool.autoPause(); //先暂停之前的播放流程
        soundPool.load(context, id, 0);
        soundPool.setOnLoadCompleteListener((s, sampleId, status) -> {
            if (status == 0) { //加载成功
                s.play(sampleId, 1f, 1f, 1, 0, 1f);
            }
        });
    }

    private void releaseSoundPool() {
        if (null == soundPool) return;
        soundPool.autoPause();
        soundPool.release();
        soundPool = null;
    }


    AbstractSportsServerImpl getSportsServiceByType(int type) {
        switch (type) {
            case SportRecord.TYPE_INDOOR:
                DeviceIndoorRunningServiceImpl deviceIndoorRunningService = new DeviceIndoorRunningServiceImpl(context);
                deviceIndoorRunningService.setRealDataListener(baseRealDataMutableLiveData::postValue);
                return deviceIndoorRunningService;
            case SportRecord.TYPE_OUTDOOR:
                DeviceOutdoorRunningServiceImpl deviceOutdoorRunningService = new DeviceOutdoorRunningServiceImpl(context);
                deviceOutdoorRunningService.setRealDataListener(baseRealDataMutableLiveData::postValue);
                deviceOutdoorRunningService.setMapLocationListener(aMapLocation -> locationRealDataMutableLiveData.postValue(new LocationRealData(aMapLocation)));
                return deviceOutdoorRunningService;
        }
        return null;
    }

    @Override
    public void onSportsInfoChange(SportsInfo sportsInfo) {
        JL_Log.e(TAG, "onSportsInfoChange", "sportsInfo ---> " + sportsInfo);
        boolean isSporting = sportsInfo.status != SportsInfo.STATUS_STOP && sportsInfo.status != SportsInfo.STATUS_FAILED;
        int sportMode = !isSporting ? HealthApplication.SPORT_MODE_IDLE : sportsInfo.type;
        ((HealthApplication) HealthApplication.getAppViewModel().getApplication()).setSportMode(sportMode);
        sportsInfoMutableLiveData.postValue(sportsInfo);
        if (sportsInfo.status == SportsInfo.STATUS_STOP) {
            playSound(R.raw.stop);
            requestRecordLiveData.postValue(new RequestRecordState(RequestRecordState.REQUEST_RECORD_STATE_START, sportsInfo.startTime, sportsInfo.type));
            new DeviceRequestRecordHandler(sportsInfo.file, new OperatCallback() {
                @Override
                public void onSuccess() {
                    requestRecordLiveData.postValue(new RequestRecordState(RequestRecordState.REQUEST_RECORD_STATE_SUCCESS, sportsInfo.startTime, sportsInfo.type));
                    //todo 上传运动记录到服务器
                    SyncTaskManager.getInstance().addTask(new UploadSportsRecordSyncTask(SyncTaskManager.getInstance()));
                }

                @Override
                public void onError(int code) {
                    requestRecordLiveData.postValue(RequestRecordState.FAILED_STATE);
                }
            }).request();
        } else if (sportsInfo.status == SportsInfo.STATUS_PAUSE) {
            playSound(R.raw.pause);
        } else if (sportsInfo.status == SportsInfo.STATUS_BEGIN) {
            playSound(R.raw.begin);
        } else if (sportsInfo.status == SportsInfo.STATUS_RESUME) {
            playSound(R.raw.resume);
        } else if (sportsInfo.status == SportsInfo.STATUS_FAILED) {
            //运动异常
        }
    }

    public MutableLiveData<RequestRecordState> getRequestRecordLiveData() {
        return requestRecordLiveData;
    }

    public static class ViewModelFactory extends ViewModelProvider.NewInstanceFactory {

        private final int type;
        private final Application mApplication;

        /**
         * Creates a {@code AndroidViewModelFactory}
         *
         * @param application an application to pass in {@link AndroidViewModel}
         */
        public ViewModelFactory(@NonNull Application application, int type) {
            this.mApplication = application;
            this.type = type;
        }


        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            try {
                return modelClass.getConstructor(Application.class, Integer.class).newInstance(mApplication, type);
            } catch (NoSuchMethodException | IllegalAccessException | InstantiationException |
                     InvocationTargetException e) {
                throw new RuntimeException("Cannot create an instance of " + modelClass, e);
            }

        }

    }
}