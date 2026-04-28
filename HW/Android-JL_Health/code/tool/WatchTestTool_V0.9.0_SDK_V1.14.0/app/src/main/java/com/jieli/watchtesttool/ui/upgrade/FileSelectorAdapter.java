package com.jieli.watchtesttool.ui.upgrade;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.jieli.jl_rcsp.util.RcspUtil;
import com.jieli.watchtesttool.R;
import com.jieli.watchtesttool.util.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件选择适配器
 *
 * @author zqjasonZhong
 * @date 2019/10/22
 */
public class FileSelectorAdapter extends BaseQuickAdapter<File, BaseViewHolder> {

    private final boolean isAllowMulti; //是否允许多选
    private final List<String> selectedFilePaths = new ArrayList<>();

    public FileSelectorAdapter(boolean isAllowMulti) {
        super(R.layout.item_file_selector);
        this.isAllowMulti = isAllowMulti;
        addChildClickViewIds(R.id.btn_delete);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, File item) {
        if (null == item) return;
        helper.getView(R.id.cl_content).setOnClickListener(v -> {
            final OnItemClickListener listener = getOnItemClickListener();
            if(null == listener) return;
            listener.onItemClick(this, v, getItemPosition(item));
        });
        helper.setText(R.id.tv_file_msg, RcspUtil.formatString("%s\t\t[%s]", item.getName(), FileUtil.formatFileSize(item.length())));
        helper.setText(R.id.tv_file_path, formatFilePath(item.getPath()));
        helper.setImageResource(R.id.iv_select_state, isSelectedFile(item) ? R.drawable.ic_check_flag_blue
                : R.drawable.ic_check_flag_gary);
    }

    public List<String> getSelectFilePaths() {
        if (selectedFilePaths.isEmpty()) return null;
        return new ArrayList<>(selectedFilePaths);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void selectFile(int index, String filePath) {
        if (index >= 0 && index < getData().size()) {
            if (selectedFilePaths.contains(filePath)) {
                selectedFilePaths.remove(filePath);
            } else {
                if (!isAllowMulti && selectedFilePaths.size() == 1) {
                    selectedFilePaths.clear();
                }
                selectedFilePaths.add(filePath);
            }
            notifyDataSetChanged();
        }
    }

    private boolean isSelectedFile(File file) {
        return null != file && selectedFilePaths.contains(file.getPath());
    }

    private String formatFilePath(String filePath) {
        if (null == filePath || filePath.isEmpty()) return "";
        filePath = filePath.replace("/storage/emulated/0", "");
        int index = filePath.lastIndexOf("/");
        if (index == -1 || index == filePath.length() - 1) {
            return filePath;
        }
        return filePath.substring(0, index);
    }
}
