package com.jieli.watchtesttool;

import com.jieli.watchtesttool.tool.logcat.LogcatBuilder;

import org.junit.Test;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testLogcatCmd() {
        LogcatBuilder logcatBuilder = new LogcatBuilder()
                .count(20)
                .fileSize(20 * 1024)
                .expr("test")
                .pid(123)
                .outPath("test.txt");
        System.out.print(logcatBuilder.toString());
    }


    @Test
    public void testBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(512);

        for (byte i = 0; i < 20; i++) {
            buffer.put(i);
        }
        byte data[] = new byte[buffer.position()];
        buffer.flip();
        buffer.get(data);
        System.out.println(Arrays.toString(data));


    }
}