package com.jieli.healthaide;

import com.jieli.jl_rcsp.model.command.watch.PushInfoDataToDeviceCmd;
import com.jieli.jl_rcsp.model.device.MessageInfo;
import com.jieli.jl_rcsp.util.CHexConver;
import com.jieli.jl_rcsp.util.RcspUtil;

import org.junit.Test;

import java.util.Calendar;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc
 * @since 2021/7/21
 */
public class MessageFormatTest {

    @Test
    public void parseMessageFormat() {
        String appName = "com.jieli.healthaide.main";
        int flag = 10;
        String title = "人们日报2333";
        String content = "党的伟大精神和光荣传统是我们的宝贵精神财富，是激励我们奋勇前进的强大精神动力。在庆祝中国共产党成立100周年大会上，习近平总书记精辟概括伟大建党精神的深刻内涵，指出：“一百年前，中国共产党的先驱们创建了中国共产党，形成了坚持真理、坚守理想，践行初心、担当使命，不怕牺牲、英勇斗争，对党忠诚、不负人民的伟大建党精神，这是中国共产党的精神之源”。习近平总书记指出“一百年来，中国共产党弘扬伟大建党精神，在长期奋斗中构建起中国共产党人的精神谱系，锤炼出鲜明的政治品格”，强调“我们要继续弘扬光荣传统、赓续红色血脉，永远把伟大建党精神继承下去、发扬光大！”\n" +
                "\n" +
                "　　回望近代中国历史，自鸦片战争以后，国家蒙辱、人民蒙难、文明蒙尘，中华民族遭受了前所未有的劫难。从那时起，实现中华民族伟大复兴，就成为中国人民和中华民族最伟大的梦想。十月革命一声炮响，给中国送来了马克思列宁主义，给正在苦苦探求救国救民道路的中国先进分子指明了方向，中国共产党应运而生。从登上中国政治舞台的那一刻起，中国共产党就把为中国人民谋幸福、为中华民族谋复兴确立为自己的初心使命，所形成的伟大建党精神成为引领我们党团结带领人民战胜前进道路上一切风险挑战的精神支柱和强大动力。历史深刻证明，中国产生了共产党，这是开天辟地的大事变，从此，中国人民开始从精神上由被动转为主动，中华民族开始艰难地但不可逆转地走向伟大复兴。\n" +
                "\n" +
                "　　精神的力量是无穷的。正是在伟大建党精神的引领下，中国共产党团结带领人民，浴血奋战、百折不挠，自力更生、发愤图强，解放思想、锐意进取，自信自强、守正创新，攻克了一个又一个看似不可攻克的难关，创造了一个又一个彪炳史册的人间奇迹，书写了中华民族几千年历史上最恢宏的史诗，中华民族迎来了从站起来、富起来到强起来的伟大飞跃，实现中华民族伟大复兴进入了不可逆转的历史进程！世界上没有哪个党像我们这样，遭遇过如此多的艰难险阻，经历过如此多的生死考验，付出过如此多的惨烈牺牲。正是在伟大建党精神的引领下，在应对各种困难挑战中，我们党锤炼了不畏强敌、不惧风险、敢于斗争、勇于胜利的风骨和品质，在极端困境中发展壮大，在濒临绝境中突出重围，在困顿逆境中毅然奋起，饱经磨难而风华正茂，从成立时只有50多名党员，到今天已经成为拥有9500多万名党员、领导着14亿多人口大国、具有重大全球影响力的世界第一大执政党！\n";
        long time = Calendar.getInstance().getTimeInMillis();
        byte[] cBuf = RcspUtil.getData(content, 439);
        int length = cBuf.length + 1;
        byte[] val = CHexConver.int2byte2(length);
        System.out.println("length = " + length + ", len = " + CHexConver.bytesToInt(val, 0, 2));
        MessageInfo messageInfo = new MessageInfo(appName, flag, title, content, time);
        byte[] data = messageInfo.beanToData();
        PushInfoDataToDeviceCmd.MessageData messageData;
        if (data.length > MessageInfo.LIMIT_PACKAGE) {
            int value = data.length / MessageInfo.LIMIT_PACKAGE;
            int num = data.length % MessageInfo.LIMIT_PACKAGE == 0 ? value : value + 1;
            int offset = 0;
            for (int seq = (num - 1); seq >= 0; seq--) {
                byte[] buf;
                int left = data.length - offset;
                if (left <= 0) {
                    break;
                }
                if (left > MessageInfo.LIMIT_PACKAGE) {
                    buf = new byte[MessageInfo.LIMIT_PACKAGE];
                } else {
                    buf = new byte[left];
                }
                System.arraycopy(data, offset, buf, 0, buf.length);
                offset += buf.length;
                messageData = new PushInfoDataToDeviceCmd.MessageData(seq, buf);
                System.out.println(messageData);
            }
        } else {
            messageData = new PushInfoDataToDeviceCmd.MessageData(0, messageInfo.beanToData());
            System.out.print(messageData);
        }
    }

}
