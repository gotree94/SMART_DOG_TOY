package com.jieli.watchtesttool.ui.file.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.jieli.watchtesttool.R;
import com.jieli.watchtesttool.ui.file.model.TestTypeItem;

import java.util.List;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc
 * @since 2022/6/24
 */
public class TestSpinnerAdapter extends BaseAdapter {
    private final Context mContext;
    private final List<TestTypeItem> mList;

    public TestSpinnerAdapter(Context context, @NonNull List<TestTypeItem> list) {
        mContext = context;
        mList = list;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        if (position >= mList.size()) return null;
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (null == convertView) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_test_type, parent, false);
            convertView.setTag(new ViewHolder(convertView));
        }
        convertUI((ViewHolder) convertView.getTag(), (TestTypeItem) getItem(position));
        return convertView;
    }

    private void convertUI(ViewHolder viewHolder, TestTypeItem item) {
        if (null == viewHolder || null == item) return;
        if (null != viewHolder.getTvType()) {
            viewHolder.getTvType().setText(item.getValue());
        }
    }

    private static class ViewHolder {
        private final TextView tvType;

        public ViewHolder(View view) {
            tvType = view.findViewById(R.id.tv_test_type);
            view.setTag(this);
        }

        public TextView getTvType() {
            return tvType;
        }
    }
}
