package com.jieli.healthaide.util;

import android.content.Context;
import android.net.Uri;
import android.util.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @ClassName: AppUtil
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2023/10/7 9:22
 */
public class AppUtil {
    /**
     * 复制assets资源
     *
     * @param context 上下文
     * @param oldPath assets路径
     * @param newPath 复制资源路径
     */
    public static void copyAssets(Context context, String oldPath, String newPath) {
        try {
            String[] fileNames = context.getAssets().list(oldPath);// 获取assets目录下的所有文件及目录名
            if (fileNames.length > 0) {// 如果是目录
                File file = new File(newPath);
                if (!file.exists()) {
                    boolean ret = file.mkdirs();// 如果文件夹不存在，则递归
                    if (!ret) return;
                }
                for (String fileName : fileNames) {
                    copyAssets(context, oldPath + File.separator + fileName, newPath + File.separator + fileName);
                }
            } else {// 如果是文件
                InputStream is = context.getAssets().open(oldPath);
                FileOutputStream fos = new FileOutputStream(new File(newPath));
                byte[] buffer = new byte[1024];
                int byteCount;
                while ((byteCount = is.read(buffer)) != -1) {// 循环从输入流读取
                    // buffer字节
                    fos.write(buffer, 0, byteCount);// 将读取的输入流写入到输出流
                }
                fos.flush();// 刷新缓冲区
                is.close();
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void copyFile(Context context, Uri uri, String outPath) {
        try {
            InputStream is = context.getContentResolver().openInputStream(uri);
//            InputStream is = new FileInputStream(uri);
            FileOutputStream fos;
            fos = new FileOutputStream(outPath);
            byte[] buffer = new byte[1024];
            int byteCount;
            while ((byteCount = is.read(buffer)) != -1) {// 循环从输入流读取
                // buffer字节
                fos.write(buffer, 0, byteCount);// 将读取的输入流写入到输出流
            }
            fos.flush();// 刷新缓冲区
            is.close();
            fos.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void copyFile(String srcPath, String outPath) {
        try {
            InputStream is = new FileInputStream(srcPath);
            FileOutputStream fos;
            fos = new FileOutputStream(outPath);
            byte[] buffer = new byte[1024];
            int byteCount;
            while ((byteCount = is.read(buffer)) != -1) {// 循环从输入流读取
                // buffer字节
                fos.write(buffer, 0, byteCount);// 将读取的输入流写入到输出流
            }
            fos.flush();// 刷新缓冲区
            is.close();
            fos.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param imgStr base64编码字符串
     * @param path   图片路径-具体到文件
     */
    public static boolean base64StrGenerateImage(String imgStr, String path) {
        if (imgStr == null)
            return false;
//        BASE64Decoder decoder = new BASE64Decoder();
        try {
// 解密
//            byte[] b = decoder.decodeBuffer(imgStr);
            byte[] b = Base64.decode(imgStr.getBytes(), Base64.DEFAULT);
// 处理数据
            for (int i = 0; i < b.length; ++i) {
                if (b[i] < 0) {
                    b[i] += 256;
                }
            }
            OutputStream out = new FileOutputStream(path);
            out.write(b);
            out.flush();
            out.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
