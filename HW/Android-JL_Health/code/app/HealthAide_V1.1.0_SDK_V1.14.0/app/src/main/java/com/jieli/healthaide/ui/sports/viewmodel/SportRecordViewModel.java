package com.jieli.healthaide.ui.sports.viewmodel;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.amap.api.maps.model.LatLng;
import com.jieli.component.thread.ThreadManager;
import com.jieli.component.utils.ValueUtil;
import com.jieli.healthaide.HealthApplication;
import com.jieli.healthaide.R;
import com.jieli.healthaide.data.db.HealthDataDbHelper;
import com.jieli.healthaide.data.entity.LocationEntity;
import com.jieli.healthaide.data.entity.SportRecord;
import com.jieli.healthaide.ui.sports.model.SportsRecordAndLocation;
import com.jieli.jl_rcsp.util.JL_Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 4/8/21
 * @desc :
 */
public class SportRecordViewModel extends AndroidViewModel {

    private static final String TAG = SportRecordViewModel.class.getSimpleName();
    public static final int PAGE_SIZE = 20;

    private final MutableLiveData<List<SportsRecordAndLocation>> recordsLiveData = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<SportsRecordAndLocation>> getRecordsLiveData() {
        return recordsLiveData;
    }

    public SportRecordViewModel(@NonNull Application application) {
        super(application);
        JL_Log.d(TAG, "init", "");
    }


    public void queryByPage(int pageSize, int offset) {
        ThreadManager.getInstance().postRunnable(() -> {
            String uid = HealthApplication.getAppViewModel().getUid();
            if (TextUtils.isEmpty(uid)) return;
            List<SportRecord> sportRecords = HealthDataDbHelper.getInstance().getSportRecordDao().selectByPage(uid, pageSize, offset);
            JL_Log.d(TAG, "queryByPage", "find sports record size = " + sportRecords.size() + "\toffset = " + offset);
            List<SportsRecordAndLocation> list = new ArrayList<>();
            try {

                for (SportRecord sportRecord : sportRecords) {
                    SportsRecordAndLocation sportsRecordAndLocation;
                    if (sportRecord.getType() == SportRecord.TYPE_OUTDOOR) {
                        LocationEntity locationEntity = HealthDataDbHelper.getInstance().getLocationDao().findByStartTime(uid, sportRecord.getStartTime());
//                        String path = createTrackBitmap(getApplication(), locationEntity);//生成预览图片
                        String path = "";
                        sportsRecordAndLocation = new SportsRecordAndLocation(sportRecord, locationEntity, path);
                    } else {
                        sportsRecordAndLocation = new SportsRecordAndLocation(sportRecord, null, null);
                    }
                    list.add(sportsRecordAndLocation);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            recordsLiveData.postValue(list);
        });

    }


    /**
     * 将gps坐标转化为图片，性能有点问题：
     * 解决思路： 运动完或者数据同步时先提前生成图片
     *
     * @param locationEntity 位置信息
     * @return 图片路径
     */
    public static String createTrackBitmap(Context context, LocationEntity locationEntity) {
        if (locationEntity == null) return null;

        //缓存
        String dirPath = context.getExternalCacheDir() + File.separator + "map_tracks_dir";
        String cachePath = dirPath + File.separator + locationEntity.getUid() + "_" + locationEntity.getStartTime() + ".track";

        File dir = new File(dirPath);
        if (!dir.exists()) {
            if (!dir.mkdir()) return null;
        }
        File file = new File(cachePath);
        if (file.exists()) {
            return cachePath;
        }

        List<List<LatLng>> list = locationEntity.toTrackData();
        if (list == null || list.isEmpty()) return null;

        int size = ValueUtil.dp2px(context, 140);
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);

        //求最大最小值
        double minX = 360, maxX = 0;
        double minY = 180, maxY = 0;
        for (List<LatLng> arr : list) {
            for (LatLng latLng : arr) {
                double x = latLng.longitude;//经度
                double y = latLng.latitude; //纬度
                minX = Math.min(minX, x);
                maxX = Math.max(maxX, x);

                minY = Math.min(minY, y);
                maxY = Math.max(maxY, y);
            }
        }


        double dX = (maxX - minX);
        double dY = (maxY - minY);

        canvas.drawColor(Color.WHITE);
        Paint paint = new Paint();
        paint.setColor(ResourcesCompat.getColor(context.getResources(), R.color.main_color, context.getTheme()));
        paint.setStrokeWidth(15);
        paint.setAntiAlias(true);
        paint.setTextSize(20);
        paint.setStyle(Paint.Style.STROKE);

        //画图 重点是对坐标进行平面转换
        for (List<LatLng> arr : list) {
            Path path = null;
            for (LatLng latLng : arr) {
                double x = (latLng.longitude - minX);//经度
                double y = (latLng.latitude - minY); //纬度
                x = dX == 0 ? 0 : x / dX * size;
                y = dY == 0 ? 0 : y / dY * size;
                if (path == null) {
                    path = new Path();
                    path.moveTo((float) x, (float) y);
                } else {
                    path.lineTo((float) x, (float) y);
                }
                canvas.drawPoint((float) x, (float) y, paint);
            }
            if (path != null) {
                canvas.drawPath(path, paint);
            }
        }


        //保存为jpg
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(cachePath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return cachePath;
    }
}
