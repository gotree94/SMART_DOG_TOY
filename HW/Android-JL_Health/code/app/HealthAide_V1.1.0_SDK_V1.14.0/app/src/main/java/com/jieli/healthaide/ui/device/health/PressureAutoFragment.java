package com.jieli.healthaide.ui.device.health;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.jieli.healthaide.R;
import com.jieli.jl_rcsp.model.device.health.AutomaticPressureDetection;
import com.jieli.jl_rcsp.model.device.health.HealthSettingInfo;
import com.kyleduo.switchbutton.SwitchButton;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/7/23
 * @desc :
 */
public class PressureAutoFragment extends BaseHealthSettingFragment {


    private Adapter adapter;
    private SwitchButton switchButton;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_pressure_auto, container, false);

        switchButton = root.findViewById(R.id.sw_pressure_auto);
        root.findViewById(R.id.tv_topbar_left).setOnClickListener(v -> requireActivity().onBackPressed());
        ((TextView) root.findViewById(R.id.tv_topbar_title)).setText(R.string.pressure_auto_testing);

        RecyclerView recyclerView = root.findViewById(R.id.rv_pressure_auto);
        DividerItemDecoration decoration = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);
        decoration.setDrawable(getResources().getDrawable(R.drawable.line_gray_1dp));
        recyclerView.addItemDecoration(decoration);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        List<Data> list = new ArrayList<>();
        list.add(new Data(R.string.pressure_auto_relax, R.drawable.dot_96c5da));
        list.add(new Data(R.string.pressure_auto_normal, R.drawable.dot_7bd083));
        list.add(new Data(R.string.pressure_auto_medium, R.drawable.dot_f3c6a5));
        list.add(new Data(R.string.pressure_auto_high, R.drawable.dot_d777777));
        adapter = new Adapter(list);
        recyclerView.setAdapter(adapter);

        switchButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AutomaticPressureDetection automaticPressureDetection = viewModel.getHealthSettingInfo().getAutomaticPressureDetection();
            automaticPressureDetection.setEnable(isChecked);
            viewModel.sendSettingCmd(automaticPressureDetection);
        });

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
         viewModel.healthSettingInfoLiveData().observe(getViewLifecycleOwner(), new Observer<HealthSettingInfo>() {
            @Override
            public void onChanged(HealthSettingInfo healthSettingInfo) {
                AutomaticPressureDetection automaticPressureDetection = healthSettingInfo.getAutomaticPressureDetection();
                switchButton.setCheckedNoEvent(automaticPressureDetection.isEnable());
            }
        });
    }

    private static class Adapter extends BaseQuickAdapter<Data, BaseViewHolder> {
        public Adapter(List<Data> list) {
            super(R.layout.item_pressure_auto, list);
        }


        @Override
        protected void convert(@NotNull BaseViewHolder holder, Data data) {
            holder.setText(R.id.tv_name, data.title);
            holder.setImageResource(R.id.iv_left, data.left);
            holder.setText(R.id.tv_value, data.min + "-" + data.max);
        }
    }

    static class Data {
        public Data(int title, int left) {
            this.left = left;
            this.title = title;
        }

        int left;
        int title;
        int min;
        int max;
    }

}
