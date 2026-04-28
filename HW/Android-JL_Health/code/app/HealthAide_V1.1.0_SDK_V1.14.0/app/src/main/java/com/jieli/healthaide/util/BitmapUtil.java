package com.jieli.healthaide.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 图像处理工具类
 * @since 2024/2/20
 */
public class BitmapUtil {

    /**
     * 填充图像背景
     *
     * @param srcBmp 原图
     * @param color  填充颜色
     * @return 填充图像
     */
    public static Bitmap fillBitmap(@NonNull Bitmap srcBmp, @ColorInt int color) {
        if (color == 0) return srcBmp;
        Bitmap destBmp = Bitmap.createBitmap(srcBmp.getWidth(), srcBmp.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(destBmp);
        canvas.drawColor(color); //填充底色
        canvas.drawBitmap(srcBmp, 0, 0, null); //绘制裁剪图像
        return destBmp;
    }

    /**
     * 裁剪圆角图像
     *
     * @param srcBmp 原图
     * @param radius 边角半径
     * @return 圆角图像
     */
    public static Bitmap clipRoundBitmap(@NonNull Bitmap srcBmp, int radius, boolean isRecycleSrcBmp) {
        Bitmap destBmp = Bitmap.createBitmap(srcBmp.getWidth(), srcBmp.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(destBmp);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        RectF rectF = new RectF(0, 0, srcBmp.getWidth(), srcBmp.getHeight());
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawRoundRect(rectF, radius, radius, paint); //绘制出裁剪区域
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(srcBmp, 0, 0, paint);
        if (isRecycleSrcBmp && !srcBmp.isRecycled()) srcBmp.recycle();
        return destBmp;
    }

    /**
     * 裁剪圆形图像
     *
     * @param srcBmp 原图
     * @return 圆形图像
     */
    public static Bitmap clipCircleBitmap(@NonNull Bitmap srcBmp, boolean isRecycleSrcBmp) {
        final int destBitmapWidth = Math.min(srcBmp.getWidth(), srcBmp.getHeight());
        Bitmap destBmp = Bitmap.createBitmap(destBitmapWidth, destBitmapWidth, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(destBmp);

        Rect rect = new Rect(0, 0, destBmp.getWidth(), destBmp.getHeight());

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);

        canvas.drawCircle(destBitmapWidth >> 1, destBitmapWidth >> 1, destBitmapWidth >> 1, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(srcBmp, rect, rect, paint);
        if (isRecycleSrcBmp && !srcBmp.isRecycled()) srcBmp.recycle();
        return destBmp;
    }

    /**
     * 裁剪圆角图像并填充颜色
     *
     * @param srcBmp 原图
     * @param radius 边角半径
     * @param color  填充颜色
     * @return 输出图像
     */
    public static Bitmap clipRoundAndFillBitmap(@NonNull Bitmap srcBmp, int radius, @ColorInt int color) {
        return clipRoundAndFillBitmap(srcBmp, radius, color, true);
    }

    /**
     * 裁剪圆角图像并填充颜色
     *
     * @param srcBmp          原图
     * @param radius          边角半径
     * @param color           填充颜色
     * @param isRecycleSrcBmp 是否释放原图
     * @return 输出图像
     */
    public static Bitmap clipRoundAndFillBitmap(@NonNull Bitmap srcBmp, int radius, @ColorInt int color, boolean isRecycleSrcBmp) {
        return fillBitmap(clipRoundBitmap(srcBmp, radius, isRecycleSrcBmp), color);
    }

    /**
     * 裁剪圆形图像并填充颜色
     *
     * @param srcBmp 原图
     * @param color  填充颜色
     * @return 输出图像
     */
    public static Bitmap clipCircleAndFillBitmap(@NonNull Bitmap srcBmp, @ColorInt int color) {
        return clipCircleAndFillBitmap(srcBmp, color, true);
    }

    /**
     * 裁剪圆形图像并填充颜色
     *
     * @param srcBmp 原图
     * @param color  填充颜色
     * @return 输出图像
     */
    public static Bitmap clipCircleAndFillBitmap(@NonNull Bitmap srcBmp, @ColorInt int color, boolean isRecycleSrcBmp) {
        return fillBitmap(clipCircleBitmap(srcBmp, isRecycleSrcBmp), color);
    }

    /**
     * Bitmap保存成文件
     *
     * @param bitmap
     * @param outputPath
     * @param quality
     * @return
     */
    public static boolean bitmapToFile(Bitmap bitmap, String outputPath, int quality) {
        if (null == bitmap || null == outputPath) return false;
        try {
            FileOutputStream outStream = new FileOutputStream(outputPath);
            int index = outputPath.lastIndexOf(File.separator);
            String filename = index != -1 ? outputPath.substring(index + 1) : "";
            Bitmap.CompressFormat format = filename.endsWith(".png") || filename.endsWith(".PNG") ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG;
            bitmap.compress(format, quality, outStream);
            outStream.flush();
            outStream.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}
