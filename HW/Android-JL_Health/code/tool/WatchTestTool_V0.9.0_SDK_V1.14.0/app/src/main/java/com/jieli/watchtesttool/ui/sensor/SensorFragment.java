package com.jieli.watchtesttool.ui.sensor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.jieli.component.utils.ToastUtil;
import com.jieli.jl_rcsp.util.CHexConver;
import com.jieli.watchtesttool.R;
import com.jieli.watchtesttool.data.db.SensorDbBase;
import com.jieli.watchtesttool.data.db.sensor.SensorEntity;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class SensorFragment extends Fragment {


    Adapter adapter;
    TextView tvName;
    TextView tvMac;
    TextView tvType;
    TextView tvData;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_sensor, container, false);
        RecyclerView recyclerView = root.findViewById(R.id.rv_sensor);
        adapter = new Adapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));


        View header = root.findViewById(R.id.header_sensor);
        tvName = header.findViewById(R.id.tv_name);
        tvMac = header.findViewById(R.id.tv_mac);
        tvType = header.findViewById(R.id.tv_type);
        tvData = header.findViewById(R.id.tv_data);

        tvName.setText("Name");
        tvMac.setText("Mac");
        tvType.setText("Type");
        tvData.setText("Data");

        tvMac.setOnClickListener(v -> {
            String[] entities = SensorDbBase.buildDb(requireContext())
                    .sensorDao()
                    .groupByMac();
            AlertDialog dialog = new AlertDialog.Builder(requireContext())
                    .setTitle("选择地址")
                    .setItems(entities, (dialog1, which) -> {
                        String mac = entities[which];
                        tvMac.setTag(mac);
                        updateData();
                    })
                    .create();
            dialog.show();
        });


        tvType.setOnClickListener(v -> {
            String[] entities = SensorDbBase.buildDb(requireContext())
                    .sensorDao()
                    .groupByType();

            AlertDialog dialog = new AlertDialog.Builder(requireContext())
                    .setTitle("选择类型")
                    .setItems(entities, (dialog1, which) -> {
                        String mac = entities[which];
                        tvType.setTag(mac);
                        updateData();
                    })
                    .create();
            dialog.show();
        });


        tvName.setOnClickListener(v -> {
            String[] entities = SensorDbBase.buildDb(requireContext())
                    .sensorDao()
                    .groupByName();
            AlertDialog dialog = new AlertDialog.Builder(requireContext())
                    .setTitle("选择名字")
                    .setItems(entities, (dialog1, which) -> {
                        String mac = entities[which];
                        tvName.setTag(mac);
                        updateData();
                    })
                    .create();
            dialog.show();
        });

        tvMac.setTag("");
        tvName.setTag("");
        tvType.setTag("");
        tvName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_right_esll, 0);
        tvMac.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_right_esll, 0);
        tvType.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_right_esll, 0);


        root.findViewById(R.id.tv_reset).setOnClickListener(v -> {
            tvMac.setTag("");
            tvName.setTag("");
            tvType.setTag("");
            updateData();
        });


        root.findViewById(R.id.tv_export).setOnClickListener(v -> {
            if (adapter.getData().size() < 1) {
                ToastUtil.showToastShort("无可导出数据");
                return;
            }
            String path = requireContext().getExternalFilesDir("") + File.separator + "sensor_log.txt";
            File file = new File(path);
            try {
                FileWriter fos = new FileWriter(file);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
                for (SensorEntity entity : adapter.getData()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(dateFormat.format(new Date(entity.getTime())));
                    sb.append("\t");

                    sb.append(entity.getDevName());
                    sb.append("\t");

                    sb.append(entity.getMac());
                    sb.append("\t");

                    sb.append(entity.getType());
                    sb.append("\t");

                    sb.append(CHexConver.byte2HexStr(entity.getData()));
                    sb.append("\n");
                    fos.write(sb.toString());
                }
                fos.close();
                ToastUtil.showToastShort("导出成功：" + path);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        });

        root.findViewById(R.id.tv_clean).setOnClickListener(v -> {
            SensorDbBase.buildDb(requireContext()).sensorDao().clean();
            updateData();
        });


        return root;
    }

    private void updateData() {
        String name = (String) tvName.getTag();
        String type = (String) tvType.getTag();
        String mac = (String) tvMac.getTag();

        List<SensorEntity> entities = SensorDbBase.buildDb(requireContext()).sensorDao().find(mac, name, type);
        adapter.setList(entities);
    }

    @Override
    public void onActivityCreated(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        updateData();
    }

    private static class Adapter extends BaseQuickAdapter<SensorEntity, BaseViewHolder> {

        public Adapter() {
            super(R.layout.item_sensor);
        }

        @Override
        protected void convert(@NotNull BaseViewHolder holder, SensorEntity entity) {
            holder.setText(R.id.tv_name, entity.getDevName());
            holder.setText(R.id.tv_mac, entity.getMac());
            holder.setText(R.id.tv_type, entity.getType() + "");
            holder.setText(R.id.tv_data, CHexConver.byte2HexStr(entity.getData()));
        }
    }
}