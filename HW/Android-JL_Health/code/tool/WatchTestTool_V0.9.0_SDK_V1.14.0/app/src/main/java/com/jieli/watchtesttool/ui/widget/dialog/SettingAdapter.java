package com.jieli.watchtesttool.ui.widget.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.jieli.watchtesttool.R;
import com.jieli.watchtesttool.data.bean.SettingItem;

import java.util.List;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc  Spinner设置适配器
 * @since 2023/1/31
 */
class SettingAdapter extends BaseAdapter {
    private final Context mContext;
    private final List<SettingItem> mList;

    private int selectedId = 0;

    public SettingAdapter(@NonNull Context context, @NonNull List<SettingItem> list) {
        mContext = context;
        mList = list;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public SettingItem getItem(int position) {
        if (position < 0 || position >= getCount()) return null;
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        SettingItem item = getItem(position);
        if (null == item) return 0;
        return item.getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (null == convertView) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_spinner_setting, parent, false);
            viewHolder = new ViewHolder(convertView);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        SettingItem item = getItem(position);
        if (null != item) {
            viewHolder.tvName.setText(item.getName());
            boolean isSelected = isSelectedItem(item);
            viewHolder.tvName.setCompoundDrawablesWithIntrinsicBounds(0, 0, isSelected ? R.drawable.ic_check_blue : 0, 0);
            viewHolder.tvName.setTextColor(isSelected ? ContextCompat.getColor(mContext, R.color.blue_558CFF) : ContextCompat.getColor(mContext, R.color.black));
        }
        return convertView;
    }

    public void updateSelectedId(int id) {
        if (selectedId != id) {
            selectedId = id;
            notifyDataSetChanged();
        }
    }

    private boolean isSelectedItem(SettingItem item) {
        if (null == item) return false;
        return item.getId() == selectedId;
    }

    private static class ViewHolder {
        private final TextView tvName;

        public ViewHolder(@NonNull View root) {
            tvName = root.findViewById(R.id.tv_setting_name);
            root.setTag(this);
        }
    }
}
