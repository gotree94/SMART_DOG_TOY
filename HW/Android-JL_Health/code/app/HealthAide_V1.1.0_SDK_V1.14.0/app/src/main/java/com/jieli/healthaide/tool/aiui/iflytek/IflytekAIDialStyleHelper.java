package com.jieli.healthaide.tool.aiui.iflytek;

import android.content.Context;
import android.content.SharedPreferences;

import com.jieli.component.utils.PreferencesHelper;
import com.jieli.healthaide.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: IflytekAIDialStyleHelper
 * @Description: AI表盘-主题选择
 * @Author: ZhangHuanMing
 * @CreateDate: 2023/10/17 9:49
 */
public class IflytekAIDialStyleHelper {
    private volatile static IflytekAIDialStyleHelper instance;
    private final String KEY_IFLYTEK_AI_DIAL_STYLE = "key_iflytek_ai_dial_style";
    private Context mContext;

    public static IflytekAIDialStyleHelper getInstance() {
        return instance;
    }

    public static void init(Context context) {
        synchronized (IflytekAIDialStyleHelper.class) {
            if (null == instance) {
                instance = new IflytekAIDialStyleHelper(context);
            }
        }
    }

    private int mCurrentStyleId;

    private final List<IflytekAIDialStyle> mIflytekAIDialStyles = new ArrayList<>();

    public IflytekAIDialStyleHelper(Context context) {
        mContext = context;
        mIflytekAIDialStyles.add(new IflytekAIDialStyle(0, 0, R.string.ai_dial_style_1, R.drawable.img_aiwatch_01));
        mIflytekAIDialStyles.add(new IflytekAIDialStyle(1, 1, R.string.ai_dial_style_2, R.drawable.img_aiwatch_02));
        mIflytekAIDialStyles.add(new IflytekAIDialStyle(2, 2, R.string.ai_dial_style_3, R.drawable.img_aiwatch_03));
        mIflytekAIDialStyles.add(new IflytekAIDialStyle(3, 3, R.string.ai_dial_style_4, R.drawable.img_aiwatch_04));
        mIflytekAIDialStyles.add(new IflytekAIDialStyle(4, 4, R.string.ai_dial_style_5, R.drawable.img_aiwatch_05));
        mIflytekAIDialStyles.add(new IflytekAIDialStyle(5, 5, R.string.ai_dial_style_6, R.drawable.img_aiwatch_06));
        SharedPreferences sharedPreferences = PreferencesHelper.getSharedPreferences(mContext);
        mCurrentStyleId = sharedPreferences.getInt(KEY_IFLYTEK_AI_DIAL_STYLE, 0);
    }

    public List<IflytekAIDialStyle> getIflytekAIDialStyles() {
        return mIflytekAIDialStyles;
    }

    public String getCurrentStyle() {
        return getStyleText(mCurrentStyleId);
    }

    public void setCurrentStyleId(int id) {
        boolean isContain = false;
        for (int i = 0; i < mIflytekAIDialStyles.size(); i++) {
            IflytekAIDialStyle style = mIflytekAIDialStyles.get(i);
            if (style.id == id) {
                isContain = true;
                break;
            }
        }
        if (isContain) {
            mCurrentStyleId = id;
            PreferencesHelper.putIntValue(mContext, KEY_IFLYTEK_AI_DIAL_STYLE, id);
        }
    }

    private String getStyleText(int id) {
        for (int i = 0; i < mIflytekAIDialStyles.size(); i++) {
            IflytekAIDialStyle style = mIflytekAIDialStyles.get(i);
            if (style.id == id) {
                return mContext.getString(style.textSrcId);
            }
        }
        return null;
    }

    public static class IflytekAIDialStyle {
        public int index;//位置索引
        public int id;//唯一ID
        public int textSrcId;//文本资源id
        public int imageSrcId;//图片资源id

        public IflytekAIDialStyle(int index, int id, int textSrcId, int imageSrcId) {
            this.index = index;
            this.id = id;
            this.textSrcId = textSrcId;
            this.imageSrcId = imageSrcId;
        }
    }
}
