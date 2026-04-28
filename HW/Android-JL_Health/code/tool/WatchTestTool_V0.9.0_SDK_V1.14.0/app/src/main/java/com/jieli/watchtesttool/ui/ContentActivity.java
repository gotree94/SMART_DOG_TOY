package com.jieli.watchtesttool.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.Fragment;

import com.jieli.component.utils.SystemUtil;
import com.jieli.watchtesttool.R;
import com.jieli.watchtesttool.databinding.ActivityContentBinding;
import com.jieli.watchtesttool.ui.base.BaseActivity;

public class ContentActivity extends BaseActivity {
    public static final String FRAGMENT_TAG = "fragment_tag";
    public static final String FRAGMENT_DATA = "fragment_data";

    private static final long MIN_START_SPACE_TIME = 1000;//两次内容页面打开的最小时间间隔

    private ActivityContentBinding mContentBinding;
    private static long mLastStartTime = 0;



    public static void startContentActivity(Context context, String fragmentCanonicalName) {
        startContentActivity(context, fragmentCanonicalName, null);
    }

    public static void startContentActivity(Context context, String fragmentCanonicalName, Bundle bundle) {
        if (null == context || null == fragmentCanonicalName || doubleStart()) return;
        Intent intent = new Intent(context, ContentActivity.class);
        intent.putExtra(ContentActivity.FRAGMENT_TAG, fragmentCanonicalName);
        intent.putExtra(ContentActivity.FRAGMENT_DATA, bundle);
        context.startActivity(intent);
    }

    public static void startContentActivityForResult(Fragment fragment, String fragmentCanonicalName, Bundle bundle, int code) {
        if (null == fragment || null == fragmentCanonicalName || doubleStart()) return;
        Intent intent = new Intent(fragment.requireContext(), ContentActivity.class);
        intent.putExtra(ContentActivity.FRAGMENT_TAG, fragmentCanonicalName);
        intent.putExtra(ContentActivity.FRAGMENT_DATA, bundle);
        fragment.startActivityForResult(intent, code);
    }

    //检测两次的打开时间间隔，判断是否重复打开页面
    private static boolean doubleStart() {
        long currentStartTime = System.currentTimeMillis();
        if (currentStartTime - mLastStartTime < MIN_START_SPACE_TIME) {
            Log.e("sen", "double start activity");
            return true;
        }
        mLastStartTime = currentStartTime;
        return false;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SystemUtil.setImmersiveStateBar(getWindow(), true);
        mContentBinding = ActivityContentBinding.inflate(getLayoutInflater());
        setContentView(mContentBinding.getRoot());
        String fragmentName = getIntent().getStringExtra(FRAGMENT_TAG);
        if (null == fragmentName) {
            finish();
            return;
        }
        Bundle bundle = getIntent().getParcelableExtra(FRAGMENT_DATA);
        replaceFragment(R.id.cl_content, fragmentName, bundle);
    }

}