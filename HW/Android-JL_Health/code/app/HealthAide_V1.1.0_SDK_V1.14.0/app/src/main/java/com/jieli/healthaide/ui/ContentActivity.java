package com.jieli.healthaide.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.fragment.app.Fragment;

import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.ActivityContentBinding;
import com.jieli.healthaide.ui.base.BaseActivity;
import com.jieli.jl_rcsp.util.JL_Log;

import java.util.HashMap;
import java.util.Map;

public class ContentActivity extends BaseActivity {
    public static final String FRAGMENT_TAG = "fragment_tag";
    public static final String FRAGMENT_DATA = "fragment_data";

    private static final long MIN_START_SPACE_TIME = 1000;//两次内容页面打开的最小时间间隔
    private static final Map<String, Long> fastClickLimit = new HashMap<>();


    public static void startContentActivity(Context context, String fragmentCanonicalName) {
        startContentActivity(context, fragmentCanonicalName, null);
    }

    public static void startContentActivity(Context context, String fragmentCanonicalName, Bundle bundle) {
        if (null == context || fastStart(fragmentCanonicalName)) return;
        Intent intent = new Intent(context, ContentActivity.class);
        intent.putExtra(ContentActivity.FRAGMENT_TAG, fragmentCanonicalName);
        intent.putExtra(ContentActivity.FRAGMENT_DATA, bundle);
        context.startActivity(intent);
    }

    public static void startContentActivityForResult(Fragment fragment, String fragmentCanonicalName, Bundle bundle, int code) {
        if (null == fragment || fastStart(fragmentCanonicalName)) return;
        Intent intent = new Intent(fragment.requireContext(), ContentActivity.class);
        intent.putExtra(ContentActivity.FRAGMENT_TAG, fragmentCanonicalName);
        intent.putExtra(ContentActivity.FRAGMENT_DATA, bundle);
        fragment.startActivityForResult(intent, code);
    }

    public static void startContentActivityForResult(Fragment fragment, String fragmentCanonicalName, Bundle bundle, ActivityResultLauncher<Intent> launcher) {
        if (null == fragment || null == launcher || fastStart(fragmentCanonicalName)) return;
        Intent intent = new Intent(fragment.requireContext(), ContentActivity.class);
        intent.putExtra(ContentActivity.FRAGMENT_TAG, fragmentCanonicalName);
        intent.putExtra(ContentActivity.FRAGMENT_DATA, bundle);
        launcher.launch(intent);
    }

    //检测两次的打开时间间隔，判断是否重复打开页面
    private static boolean fastStart(String fragmentName) {
        if (null == fragmentName) return true;  //错误参数，直接不予处理
        Long startTime = fastClickLimit.get(fragmentName);
        long currentStartTime = System.currentTimeMillis();
        if (startTime != null && currentStartTime - startTime < MIN_START_SPACE_TIME) {  //判断属于快速点击，不予处理
            JL_Log.d("ContentActivity", "fastStart", "fast start. " + fragmentName);
            return true;
        }
        fastClickLimit.put(fragmentName, currentStartTime);
        return false;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        ActivityContentBinding mContentBinding = ActivityContentBinding.inflate(getLayoutInflater());
        setContentView(mContentBinding.getRoot());
        setWindowStatus(R.id.cl_content, true);
        String fragmentName = getIntent().getStringExtra(FRAGMENT_TAG);
        if (null == fragmentName) {
            finish();
            return;
        }
        Bundle bundle = getIntent().getParcelableExtra(FRAGMENT_DATA);
        replaceFragment(R.id.cl_content, fragmentName, bundle);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        fastClickLimit.clear();
    }
}