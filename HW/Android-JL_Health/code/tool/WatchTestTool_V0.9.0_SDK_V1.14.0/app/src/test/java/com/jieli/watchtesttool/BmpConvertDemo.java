package com.jieli.watchtesttool;

import com.jieli.bmp_convert.BmpConvert;
import com.jieli.bmp_convert.OnConvertListener;

import org.junit.Test;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 自定义背景文件转换测试
 * @since 2021/12/16
 */
public class BmpConvertDemo {

    /**
     * bitmap图像转换
     *
     * @param inPath 输入文件路径<p>例如：in.png ,in.jpg</p>
     * @param outPath 输出文件路径<p>例如:out.res, out</p>
     */
    @Test
    private void convertPhoto(String inPath, String outPath) {
        //1.初始化图片转换对象
        BmpConvert bmpConvert = new BmpConvert();
        //2.开始图像转换
        bmpConvert.bitmapConvert(inPath, outPath, new OnConvertListener() {
            //回调转换开始
            //path: 输入文件路径
            @Override
            public void onStart(String path) {

            }

            //回调转换结束
            //result： 转换结果
            //output： 输出文件路径
            @Override
            public void onStop(boolean result, String output) {

            }
        });
        //3.不需要使用图片转换功能时，需要释放图片转换对象
//        bmpConvert.release();
    }
}
