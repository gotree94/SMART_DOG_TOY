package com.jieli.watchtesttool.tool.test.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jieli.component.utils.ValueUtil;
import com.jieli.watchtesttool.R;
import com.jieli.watchtesttool.tool.test.filetask.ReadFileByClusterTask;
import com.jieli.watchtesttool.ui.base.BaseFragment;

import java.io.File;
import java.util.Objects;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 6/9/21
 * @desc :
 */
public class CacheFileFragment extends BaseFragment {


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_watch_vrow, container, false);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        TextView textView = requireView().findViewById(R.id.tv_title);
        textView.setText("读取回来的文件");

        LinearLayout parent = requireView().findViewById(R.id.ll_watch_brow);
        parent.removeAllViews();
        File dir = new File(ReadFileByClusterTask.READ_FILE_DIR);
        if (dir.listFiles() == null) return;
        for (File f : Objects.requireNonNull(dir.listFiles())) {
            parent.addView(createTestItem(f.getName(), v -> {
            }));
        }
    }


    public View createTestItem(String name, View.OnClickListener listener) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ValueUtil.dp2px(requireContext(), 44));
        Button btn = new Button(requireContext());
        btn.setOnClickListener(listener);
        btn.setText(name);
        btn.setLayoutParams(lp);
        return btn;
    }


}
