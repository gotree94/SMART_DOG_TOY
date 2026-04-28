package com.jieli.watchtesttool.util;

import android.Manifest;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.jieli.jl_rcsp.util.JL_Log;
import com.jieli.jl_rcsp.util.RcspUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import permissions.dispatcher.PermissionUtils;

/**
 * FileUtil
 *
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 文件工具类
 * @since 2024/11/25
 */
public class FileUtil {
    private static final String TAG = FileUtil.class.getSimpleName();
    /**
     * 杰理手表文件夹
     */
    public static final String DIR_JL_WATCH = "JieLi_Watch";

    /**
     * 是否分区存储
     *
     * @return boolean 结果
     */
    public static boolean isScopeStorage() {
        return Environment.getExternalStorageDirectory().equals(Environment.getRootDirectory());
    }

    /**
     * 文件是否存在文件夹
     *
     * @param context  Context 上下文
     * @param fileName String 文件名
     * @return boolean 结果
     */
    public static boolean isFileInDownload(@NonNull Context context, @NonNull String fileName) {
        if (!PermissionUtils.hasSelfPermissions(context, Manifest.permission.READ_EXTERNAL_STORAGE))
            return false;
        String selection = MediaStore.Downloads.DISPLAY_NAME + " = ?";
        String[] args = new String[]{fileName};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                Cursor cursor = context.getContentResolver().query(MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                        null, selection, args, null);
                boolean result = false;
                if (null != cursor) {
                    while (cursor.moveToFirst()) {
                        String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Downloads.DISPLAY_NAME));
                        result = fileName.equals(name);
                        if (result) break;
                    }
                    cursor.close();
                    if (result) return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            return new File(getDownloadFilePath(fileName)).exists();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取下载文件路径
     *
     * @param fileName String 文件名
     * @return String 下载文件路径
     */
    public static String getDownloadFilePath(@NonNull String fileName) {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()
                + File.separator + DIR_JL_WATCH + File.separator + fileName;
    }

    /**
     * 获取下载文件夹
     *
     * @return Uri 下载文件夹
     */
    @NonNull
    public static Uri getDownloadDirectoryByUri() {
        return Uri.parse("content://com.android.externalstorage.documents/tree/primary%3ADownload");
    }

    /**
     * 通过文件路径获取对应路径
     *
     * @param context Context 上下文
     * @param path    String 文件路径
     * @return Uri 路径
     */
    public static Uri getUriByPath(@NonNull Context context, @NonNull String path) {
        if (path.isEmpty()) {
            return null;
        }
        return getUriByFile(context, new File(path));
    }

    /**
     * 通过文件获取对应路径
     *
     * @param context Context 上下文
     * @param file    File 文件
     * @return Uri 路径
     */
    public static Uri getUriByFile(@NonNull Context context, @NonNull File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
        }
        return Uri.fromFile(file);
    }

    /**
     * 格式化文件大小
     *
     * @param fileSize long 文件大小
     * @return String 格式化内容
     */
    public static String formatFileSize(long fileSize) {
        if (fileSize < 1024) return fileSize + " Bytes";
        float value = fileSize / 1024f;
        if (value < 1024) return RcspUtil.formatString("%.1f KB", value);
        value /= 1024f;
        if (value < 1024) return RcspUtil.formatString("%.1f MB", value);
        value /= 1024f;
        return RcspUtil.formatString(".1f GB", value);
    }

    /**
     * 删除文件
     *
     * @param filePath String 文件路径
     * @return boolean 结果
     */
    public static boolean deleteFile(@NonNull String filePath) {
        return deleteFile(new File(filePath));
    }

    /**
     * 删除文件
     *
     * @param file File 文件
     * @return boolean 结果
     */
    public static boolean deleteFile(@NonNull File file) {
        if (!file.exists()) return false;
        if (file.isFile()) {
            return file.delete();
        }
        File[] childFiles = file.listFiles();
        if (null == childFiles) {
            //空文件夹，直接删除
            return file.delete();
        }
        for (File child : childFiles) {
            if (!deleteFile(child)) {
                //删除文件失败
                return false;
            }
        }
        //已删除子文件，空文件夹，删除
        return file.delete();
    }

    public static File copyFileFromUri(@NonNull Context context, @NonNull Uri uri,
                                       @NonNull String outputDirPath, String... suffixs) {
        try {
            String fileName = readFileNameFromUri(context, uri);
            if (null == fileName) return null;
            if (suffixs != null) {
                boolean isTargetFile = false;
                for (String suffix : suffixs) {
                    if (fileName.toLowerCase().endsWith(suffix.toLowerCase())) {
                        isTargetFile = true;
                        break;
                    }
                }
                if (!isTargetFile) return null;
            }
            String outputFilePath = outputDirPath + File.separator + fileName;
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (null == inputStream) return null;
            FileOutputStream outputStream = new FileOutputStream(outputFilePath);
            boolean isCopyOk = false;
            try {
                int len;
                byte[] buf = new byte[2048];
                while ((len = inputStream.read(buf)) != -1) {
                    byte[] data = Arrays.copyOfRange(buf, 0, len);
                    outputStream.write(data);
                }
                isCopyOk = true;
            } catch (IOException e) {
                JL_Log.w(TAG, "copyFileFromUri", "IO Exception. " + e);
            } finally {
                inputStream.close();
                outputStream.close();
            }
            return isCopyOk ? new File(outputFilePath) : null;
        } catch (IOException e) {
            JL_Log.w(TAG, "copyFileFromUri", "IO Exception. ---> " + e);
        }
        return null;
    }

    public static String readFileNameFromUri(@NonNull Context context, @NonNull Uri uri) {
        String fileName = null;
        try {
            if ("content".equalsIgnoreCase(uri.getScheme())) {
                Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
                if (null == cursor) return null;
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {
                        fileName = cursor.getString(index);
                    }
                    JL_Log.d(TAG, "readFileNameFromUri", "index : " + index + ", fileName : " + fileName);
                }
                cursor.close();
            } else if ("file".equalsIgnoreCase(uri.getScheme())) {
                String path = uri.getPath();
                if (null == path) return fileName;
                fileName = new File(uri.getPath()).getName();
                JL_Log.d(TAG, "readFileNameFromUri", "file way, fileName : " + fileName);
            }
        } catch (Exception e) {
            JL_Log.w(TAG, "readFileNameFromUri", "Exception. ---> " + e);
        }
        return fileName;
    }
}
