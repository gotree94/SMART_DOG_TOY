package com.jieli.healthaide.ui.device.contact;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentContactChoseBinding;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.ui.widget.CustomTextWatcher;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/17/21 9:28 AM
 * @desc :
 */
public class ContactChoseFragment extends BaseFragment {

    public static final String KEY_FILTER_LIST = "KEY_FILTER_LIST";
    public static final String KEY_RETURN_LIST = "KEY_RETURN_LIST";
    private FragmentContactChoseBinding mBinding;
    private ContactAdapter contactAdapter;

    private ContactChoseViewModel mViewModel;

    private int maxCount = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentContactChoseBinding.inflate(inflater, container, false);
        mBinding.viewTopbar.tvTopbarLeft.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
        mBinding.viewTopbar.tvTopbarLeft.setTextColor(getResources().getColor(R.color.auxiliary_widget));
        mBinding.viewTopbar.tvTopbarRight.setTextColor(getResources().getColor(R.color.auxiliary_widget));
        mBinding.viewTopbar.tvTopbarRight.setVisibility(View.VISIBLE);
        mBinding.viewTopbar.tvTopbarLeft.setText(R.string.cancel);
        mBinding.viewTopbar.tvTopbarRight.setText(R.string.add);
        mBinding.viewTopbar.tvTopbarRight.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        mBinding.viewTopbar.tvTopbarTitle.setText(R.string.unchose);

        mBinding.viewTopbar.tvTopbarRight.setOnClickListener(v -> {
            List<Contact> contacts = new ArrayList<>();
            for (IndexContactData contactData : contactAdapter.getData()) {
                if (contactData.contact != null && contactData.contact.isSelect()) {
                    contactData.contact.setSelect(false);
                    contacts.add(contactData.contact);
                }
            }
            if (contacts.isEmpty()) {
                showTips(getString(R.string.tip_please_chose_contact));
                return;
            }
            back(300, () -> {
                String json = new Gson().toJson(contacts);
                Intent intent = new Intent();
                intent.putExtra(KEY_RETURN_LIST, json);
                requireActivity().setResult(Activity.RESULT_OK, intent);
            });
        });
        mBinding.viewTopbar.tvTopbarLeft.setOnClickListener(v -> back());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
        mBinding.rvContactChose.setLayoutManager(linearLayoutManager);
        contactAdapter = new ContactAdapter();

        mBinding.rvContactChose.setAdapter(contactAdapter);
        contactAdapter.setOnItemClickListener((adapter, view, position) -> {
            IndexContactData data = contactAdapter.getItem(position);
            if (data.type == ContactAdapter.TYPE_INDEX) return;

            int count = 0;
            for (IndexContactData contactData : contactAdapter.getData()) {
                if (contactData.contact != null && contactData.contact.isSelect()) {
                    count++;
                }
            }
            boolean select = !data.contact.isSelect();
            if (select && count >= maxCount) {
                showTips(getString(R.string.tip_max_contact_err, maxCount));
                return;
            }
            count = count + (select ? 1 : -1);
            data.contact.setSelect(select);
            contactAdapter.notifyItemChanged(position);
            if (count <= 0) {
                mBinding.viewTopbar.tvTopbarTitle.setText(getString(R.string.unchose));
            } else {
                mBinding.viewTopbar.tvTopbarTitle.setText(getString(R.string.format_contact_chose_count, count));
            }


        });

        //索引联动
        mBinding.aivContact.setListener((index, text) -> {
            for (IndexContactData data : contactAdapter.getData()) {
                if (data.type == 0 && data.index.equalsIgnoreCase(text)) {
                    linearLayoutManager.scrollToPositionWithOffset(contactAdapter.getItemPosition(data), 0);
                    break;
                }
            }
        });

        mBinding.etContactSearch.addTextChangedListener(new CustomTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);
                mViewModel.searchByText(s.toString());
                mBinding.ivClearContactSearch.setVisibility(TextUtils.isEmpty(s.toString()) ? View.INVISIBLE : View.VISIBLE);
            }
        });


        mBinding.etContactSearch.setOnFocusChangeListener((v, hasFocus) -> {
            mBinding.tvContactCancelSearch.setVisibility(hasFocus ? View.VISIBLE : View.GONE);
            mBinding.etContactSearch.setBackgroundResource(hasFocus ? R.drawable.bg_et_white_shape_blue_stroke : R.drawable.bg_et_white_shape);
        });

        mBinding.tvContactCancelSearch.setOnClickListener(v -> {
            mBinding.ivClearContactSearch.setVisibility(View.GONE);
            final View focusView = requireActivity().getCurrentFocus();
            if (focusView != null) {
                InputMethodManager methodManager = ((InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE));
                methodManager.hideSoftInputFromWindow(focusView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
            mBinding.etContactSearch.clearFocus();
            mViewModel.searchByText("");

        });

        mBinding.ivClearContactSearch.setOnClickListener(v -> mBinding.etContactSearch.setText(""));


        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        mViewModel = new ViewModelProvider(this).get(ContactChoseViewModel.class);
        mViewModel.mConnectionDataMLD.observe(getViewLifecycleOwner(), deviceConnectionData -> {
            if (deviceConnectionData.getStatus() != BluetoothConstant.CONNECT_STATE_CONNECTED) {
                requireActivity().finish();
            }
        });
        mViewModel.contactLiveData.observe(getViewLifecycleOwner(), list -> {
            mBinding.viewTopbar.tvTopbarTitle.setText(R.string.unchose);
            if (list == null || list.isEmpty()) {
                View emptyView = LayoutInflater.from(requireContext()).inflate(R.layout.item_contact_chose_empty_view, null);
                contactAdapter.setEmptyView(emptyView);
                mBinding.aivContact.setVisibility(View.INVISIBLE);
            } else {
                mBinding.aivContact.setVisibility(View.VISIBLE);
            }
            contactAdapter.setList(list);
        });
        if (getArguments() == null || TextUtils.isEmpty(requireArguments().getString(KEY_FILTER_LIST))) {
            mViewModel.loadContacts(null);
            return;
        }
        String json = requireArguments().getString(KEY_FILTER_LIST);
        List<Contact> contacts = new Gson().fromJson(json, new TypeToken<List<Contact>>() {
        }.getType());
        int itemCount = null == contacts ? 0 : contacts.size();
        maxCount = Math.max(ContactFragment.MAC_COUNT - itemCount, 0);
        mViewModel.loadContacts(contacts);
    }

    private static class ContactAdapter extends BaseMultiItemQuickAdapter<IndexContactData, BaseViewHolder> {
        static final int TYPE_INDEX = 0;
        static final int TYPE_CONTACT = 1;

        public ContactAdapter() {
            addItemType(TYPE_INDEX, R.layout.item_contact_chose_index);
            addItemType(TYPE_CONTACT, R.layout.item_contact_chose);

        }

        @Override
        protected void convert(@NotNull BaseViewHolder holder, IndexContactData data) {
            if (data.type == 0) {
                holder.setText(R.id.tv_contact_chose_index, data.index);
            } else {
                View root = holder.getView(R.id.cl_contact_chose_root);
                holder.setText(R.id.tv_contact_name, data.contact.getName());
                holder.setText(R.id.tv_contact_number, data.contact.getNumber());
                holder.getView(R.id.iv_contact_end).setSelected(data.contact.isSelect());
                root.setBackgroundResource(data.bgRes);
                root.setSelected(data.contact.isSelect());

                ImageView iv = holder.getView(R.id.iv_contact_avatar);
                if (!TextUtils.isEmpty(data.contact.getPhoneUri())) {
                    Glide.with(getContext()).load(data.contact.getPhoneUri()).circleCrop().into(iv);
                } else {
                    iv.setImageResource(R.drawable.ic_contact_default_avatar);
                }

                int pos = getItemPosition(data);
                if (data.type == 0) {
                    return;
                }
                IndexContactData last = getItem(pos - 1);
                if (pos == getData().size() - 1) {
                    root.setBackgroundResource(last.type == 0 ? R.drawable.bg_contact_chose_item_all_shape : R.drawable.bg_contact_chose_item_bottom_shape);
                    holder.setGone(R.id.view_line_contact, true);
                    return;
                }
                IndexContactData next = getItem(pos + 1);
                holder.setGone(R.id.view_line_contact, next.type == 0);
                if (last.type == 0 && next.type == 0) {
                    root.setBackgroundResource(R.drawable.bg_contact_chose_item_all_shape);
                } else if (last.type == 0 && next.type == 1) {
                    root.setBackgroundResource(R.drawable.bg_contact_chose_item_top_shape);
                } else if (last.type == 1 && next.type == 0) {
                    root.setBackgroundResource(R.drawable.bg_contact_chose_item_bottom_shape);
                } else if (last.type == 1 && next.type == 1) {
                    root.setBackgroundResource(R.drawable.bg_contact_chose_item_center_shape);
                }
            }

        }
    }


}
