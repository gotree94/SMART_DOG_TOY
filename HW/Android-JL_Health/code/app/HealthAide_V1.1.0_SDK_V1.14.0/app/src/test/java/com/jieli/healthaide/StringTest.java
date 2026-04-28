package com.jieli.healthaide;

import com.jieli.healthaide.data.entity.SportRecord;
import com.jieli.jl_rcsp.exception.ParseDataException;
import com.jieli.jl_rcsp.model.command.watch.SportsInfoStatusSyncCmd;
import com.jieli.jl_rcsp.util.CHexConver;

import org.junit.Test;

import java.io.UnsupportedEncodingException;

public class
StringTest {
    @Test
    public void testString() {
        String name = "a{|}b<c()  \"\"df~jk.#$+/efg.1::23hijhlm-^]op\\[qr?@s>=<;:w.-。，rr.txt";
        //不能包含\ / : " < > . space
        //去掉多余的标点字符
        int lastIndex = name.lastIndexOf(".");
        String reg = "[\\x00-\\x1f\\x2f\\x3a\\x3c\\x3e\\x5c\\x22\\x2e]";
        String regname = name.substring(0, lastIndex).replaceAll(reg, "") + name.substring(lastIndex);

        System.out.println("name = " + regname);


    }

    @Test
    public void test() {
        byte[] data = CHexConver.hexStr2Bytes("03 38 7F 15 6A 00 01 00 0A F0 02 00 4E 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ");
        try {
            SportsInfoStatusSyncCmd.FirmwareStopSportsParam param = new SportsInfoStatusSyncCmd.FirmwareStopSportsParam(data);
            System.out.println("param : " + param.getFileId());
        } catch (ParseDataException e) {
            System.out.println("ParseDataException : " + e.getMessage());
        }
    }

    @Test
    public void testFileName() {
        String name = "后来I am\\/<>:\"   .mp3";
        getLongNameData(name, 0);
        getLongNameData(name, 1);
    }

    public byte[] getLongNameData(String name, int renameTime) {
        //去掉多余的标点字符
        //不能包含\ / : " < > . space
        String reg = "[\\x00-\\x1f\\x2f\\x3a\\x3c\\x3e\\x5c\\x22]";
        name = name.replaceAll(reg, "");
        int lastDotIndex = name.lastIndexOf('.');//提示文件后缀
        String firstName = lastDotIndex != -1 ? name.substring(0, lastDotIndex) : name;
        firstName = firstName.replaceAll("\\.", "");//去掉多余的字符
        String lastName = lastDotIndex != -1 ? name.substring(lastDotIndex) : "";
        String timeString = "";
        if (renameTime > 0) {
            timeString = "000" + renameTime;
            timeString = timeString.substring(timeString.length() - 3);
        }
        String lenName = firstName + timeString + lastName;
        System.out.println("sen\t" + "获取文件名称 " + "\tretryName = " + renameTime + "\tname = " + name + "\tlenName = " + lenName);

        byte[] headerData = "\\U".getBytes();
        byte[] nameData = new byte[0];
        try {
            nameData = lenName.getBytes("UTF-16LE");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        byte[] data = new byte[headerData.length + nameData.length];
        System.arraycopy(headerData, 0, data, 0, headerData.length);
        System.arraycopy(nameData, 0, data, headerData.length, nameData.length);
        System.out.println(CHexConver.byte2HexStr(data));
        return data;
    }

    @Test
    public void testInstance() {
        Integer a = null;
        if (Math.random() > 0.80) {
            a = 2;
        }
        if (a instanceof Integer) {
            System.out.println("test:" + (a instanceof Number));
        } else {
            System.out.println("test 1:" + (null instanceof Number));
        }
    }


    @Test
    public void testSportsRecord() {
        byte[] data = CHexConver.hexStr2Bytes("010405EE20807700E0000004000004E30CC92E01047800000001047800000001047800000001047800646E010478004C86010478006C99010478001CA20104780010A901047800108101047800728301047800889001047800D28F01047800244E0104780099190104780058FD010478004025010478004E0B01047800193701047800763301047800000001047800000001047800000001047800000001047800000001047800000001047800EA2401047800000001047800E4A30104780028DB010478001BD00104780074E802048D0DC92E00048D0DC92EFF048E0DC92EFA008E0DC92E9C0667007C0100000000");

        SportRecord.from(data);
    }


    @Test
    public void testSportDataInfo() {
        byte[] data = CHexConver.hexStr2Bytes("00 00 00 00 00 00 00 00 00 00 00 03 E8 00 00 00 00 00 00 00 D3");

        SportsInfoStatusSyncCmd.ReadRealDataResponse response = null;
        try {
            response = new SportsInfoStatusSyncCmd.ReadRealDataResponse(data);
        } catch (ParseDataException e) {
            e.printStackTrace();
        }

        System.out.println("result === > " + response);
    }
}
