package com.jieli.healthaide.ui.device.contact;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.module.DraggableModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.component.utils.ValueUtil;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentContactBinding;
import com.jieli.healthaide.ui.ContentActivity;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.ui.dialog.WaitingDialog;
import com.jieli.jl_dialog.Jl_Dialog;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;


/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/16/21 11:42 AM
 * @desc :
 */
@RuntimePermissions
public class ContactFragment extends BaseFragment {

    static final int MAC_COUNT = 10;
    private static final int CODE_ADD_CONTACT = 0x12;
    private ContactViewModel mViewModel;
    private FragmentContactBinding mBinding;
    ContactAdapter contactAdapter;

    private WaitingDialog mWaitingDialog;

    public static ContactFragment newInstance() {
        return new ContactFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = FragmentContactBinding.inflate(inflater, container, false);

        mBinding.viewTopbar.tvTopbarLeft.setOnClickListener(v -> requireActivity().onBackPressed());
        mBinding.viewTopbar.tvTopbarTitle.setText(R.string.contacts);
        mBinding.viewTopbar.tvTopbarRight.setVisibility(View.VISIBLE);
        mBinding.viewTopbar.tvTopbarRight.setTextSize(15);
        mBinding.viewTopbar.tvTopbarRight.setTextColor(getResources().getColor(R.color.auxiliary_widget));
        mBinding.viewTopbar.tvTopbarRight.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        mBinding.viewTopbar.tvTopbarLeft.setTextSize(15);
        mBinding.viewTopbar.tvTopbarLeft.setTextColor(getResources().getColor(R.color.auxiliary_widget));


        mBinding.rvContact.setLayoutManager(new LinearLayoutManager(requireContext()));
        contactAdapter = new ContactAdapter();
        mBinding.rvContact.setAdapter(contactAdapter);
        View emptyView = inflater.inflate(R.layout.item_contact_empty_view, null);
        emptyView.findViewById(R.id.btn_add_contact).setOnClickListener(v -> tryToRequestContactPermission());
        contactAdapter.setEmptyView(emptyView);
        contactAdapter.setUseEmpty(true);
        contactAdapter.setFooterView(getFooterView());
        contactAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (contactAdapter.mode == ContactAdapter.MODE_DELETE) {
                contactAdapter.getItem(position).setSelect(!contactAdapter.getItem(position).isSelect());
                contactAdapter.notifyItemChanged(position);
                refreshDeleteTitle();
            }
        });
        resetTopView();
        return mBinding.getRoot();
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(ContactViewModel.class);
        mViewModel.contactLiveData.observe(getViewLifecycleOwner(), contacts -> {
            contactAdapter.setList(contacts);
            resetTopView();
        });
        mViewModel.mConnectionDataMLD.observe(getViewLifecycleOwner(), deviceConnectionData -> {
            if (deviceConnectionData.getStatus() != BluetoothConstant.CONNECT_STATE_CONNECTED) {
                requireActivity().finish();
            }
        });
        mViewModel.stateLiveData.observe(getViewLifecycleOwner(), state -> {
            switch (state) {
                case ContactViewModel.STATE_READ_CONTACT:
                case ContactViewModel.STATE_UPDATE_CONTACT:
                    showWaitDialog();
                    break;
                case ContactViewModel.STATE_UPDATE_CONTACT_ERROR:
                    dismissWaitDialog();
                    showTips(R.string.tip_update_contact_failed);
                    break;
                case ContactViewModel.STATE_READ_CONTACT_ERROR:
                    dismissWaitDialog();
                    showTips(R.string.tip_sync_contact_failed);
                    break;
                case ContactViewModel.STATE_UPDATE_CONTACT_FINISH:
                case ContactViewModel.STATE_READ_CONTACT_FINISH:
                    dismissWaitDialog();
                    break;

                case ContactViewModel.STATE_READ_CONTACT_CANCEL:
                    showTips(R.string.tip_cancel_get_contact);
                    dismissWaitDialog();
                    break;
                case ContactViewModel.STATE_UPDATE_CONTACT_CANCEL:
                    showTips(R.string.tip_cancel_update_contact);
                    dismissWaitDialog();
                    break;
                case ContactViewModel.START_ERROR_IN_CALLING:
                    showTips(getString(R.string.call_phone_error_tips));
                    break;
            }

        });

        mViewModel.readDeviceContacts();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK || requestCode != CODE_ADD_CONTACT) return;
        if (data == null) return;
        String json = data.getStringExtra(ContactChoseFragment.KEY_RETURN_LIST);
        if (TextUtils.isEmpty(json)) return;
        List<Contact> contacts = new Gson().fromJson(json, new TypeToken<List<Contact>>() {
        }.getType());

        List<Contact> list = new ArrayList<>();
        try {
            for (Contact contact : contactAdapter.getData()) {
                list.add((Contact) contact.clone());
            }
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        list.addAll(contacts);
        mViewModel.updateContact(list);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ContactFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    private void showMenuDialog() {

        int width = ValueUtil.dp2px(requireContext(), 156);
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.menu_contact, null);
        PopupWindow popupWindow = new PopupWindow(view);
//        popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setWidth(width);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);

        popupWindow.setOutsideTouchable(false);
        popupWindow.setFocusable(true);
        view.findViewById(R.id.tv_contact_menu_add).setOnClickListener(v -> {
            tryToRequestContactPermission();
            popupWindow.dismiss();
        });

        view.findViewById(R.id.tv_contact_menu_sort).setOnClickListener(v -> {
            mBinding.viewTopbar.tvTopbarRight.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
            mBinding.viewTopbar.tvTopbarRight.setText(R.string.finish);
            mBinding.viewTopbar.tvTopbarRight.setTextColor(ResourcesCompat.getColor(getResources(), R.color.auxiliary_widget, requireActivity().getTheme()));
            popupWindow.dismiss();
            contactAdapter.setMode(ContactAdapter.MODE_SORT);
            mBinding.viewTopbar.tvTopbarRight.setOnClickListener(v1 -> {
                mViewModel.updateContact(contactAdapter.getData());
                resetTopView();
            });
        });

        view.findViewById(R.id.tv_contact_menu_delete).setOnClickListener(v -> {
            mBinding.btnDeleteContact.setVisibility(View.GONE);
            mBinding.viewTopbar.tvTopbarRight.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
            mBinding.viewTopbar.tvTopbarRight.setText(R.string.delete);
            mBinding.viewTopbar.tvTopbarLeft.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
            mBinding.viewTopbar.tvTopbarLeft.setText(R.string.cancel);
            contactAdapter.setMode(ContactAdapter.MODE_DELETE);
            mBinding.viewTopbar.tvTopbarRight.setTextColor(ResourcesCompat.getColor(getResources(), R.color.auxiliary_error, requireActivity().getTheme()));
            popupWindow.dismiss();
            mBinding.viewTopbar.tvTopbarRight.setOnClickListener(v1 -> {
                if (contactAdapter.getSelectCount() < 1) {
                    showTips(getString(R.string.tip_please_chose_contact));
                    return;
                }
                showDeleteDialog();
            });
            mBinding.viewTopbar.tvTopbarLeft.setOnClickListener(v12 -> {
                resetTopView();
                List<Contact> contacts = contactAdapter.getData();
                for (Contact contact : contacts) {
                    contact.setSelect(false);
                }
                contactAdapter.setList(contacts);
            });
            refreshDeleteTitle();
        });

        int xOff = width - mBinding.viewTopbar.tvTopbarRight.getWidth() - ValueUtil.dp2px(requireContext(), 8);
        popupWindow.showAsDropDown(mBinding.viewTopbar.tvTopbarRight, -xOff, -(mBinding.viewTopbar.tvTopbarRight.getWidth() / 2) + ValueUtil.dp2px(requireContext(), 12));
    }

    @NeedsPermission({
            Manifest.permission.READ_CONTACTS
    })
    public void toAddContactFragment() {
        if (contactAdapter.getData().size() >= MAC_COUNT) {
            showTips(getString(R.string.tip_max_contact_err, MAC_COUNT));
            resetTopView();
            return;
        }

        List<Contact> contacts = contactAdapter.getData();
        String json = new Gson().toJson(contacts);
        Bundle bundle = new Bundle();
        bundle.putString(ContactChoseFragment.KEY_FILTER_LIST, json);
        ContentActivity.startContentActivityForResult(this, ContactChoseFragment.class.getCanonicalName(), bundle, CODE_ADD_CONTACT);
        resetTopView();
    }

    @OnShowRationale({
            Manifest.permission.READ_CONTACTS
    })
    public void showRelationForLocationPermission(PermissionRequest request) {
        showContactPermissionDialog(request);
    }

    @OnNeverAskAgain({
            Manifest.permission.READ_CONTACTS
    })
    public void onLocationNeverAskAgain() {
        showContactPermissionDialog(null);
    }

    private void resetTopView() {
        mBinding.viewTopbar.tvTopbarRight.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_more, 0);
        mBinding.viewTopbar.tvTopbarRight.setText("");
        mBinding.viewTopbar.tvTopbarLeft.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_back_black, 0, 0, 0);
        mBinding.viewTopbar.tvTopbarLeft.setText("");
        mBinding.viewTopbar.tvTopbarTitle.setText(R.string.contacts);

        mBinding.viewTopbar.tvTopbarRight.setOnClickListener(v -> showMenuDialog());
        mBinding.viewTopbar.tvTopbarLeft.setOnClickListener(v -> requireActivity().onBackPressed());
        mBinding.btnDeleteContact.setVisibility(View.GONE);
        contactAdapter.setMode(ContactAdapter.MODE_NORMAL);
        mBinding.viewTopbar.tvTopbarRight.setVisibility(!contactAdapter.getData().isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void refreshDeleteTitle() {
        mBinding.viewTopbar.tvTopbarTitle.setText(getString(R.string.format_contact_chose_count, contactAdapter.getSelectCount()));
    }


    private void showDeleteDialog() {

        TextView textView = new TextView(requireContext());
        textView.setText(getString(R.string.ask_delete_contact));
        textView.setTextSize(16);
        textView.setTextColor(ResourcesCompat.getColor(getResources(), R.color.text_important_color, requireActivity().getTheme()));
        textView.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(0, ValueUtil.dp2px(requireContext(), 32), 0, ValueUtil.dp2px(requireContext(), 32));
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(lp);
        Jl_Dialog.builder()
                .contentLayoutView(textView)
                .left(getString(R.string.cancel))
                .leftColor(ResourcesCompat.getColor(getResources(), R.color.auxiliary_widget, requireActivity().getTheme()))
                .rightColor(ResourcesCompat.getColor(getResources(), R.color.auxiliary_widget, requireActivity().getTheme()))
                .right(getString(R.string.delete))
                .leftClickListener((view, dialogFragment) -> dialogFragment.dismiss())
                .rightClickListener((view, dialogFragment) -> {
                    List<Contact> contacts = new ArrayList<>();
                    for (Contact contact : contactAdapter.getData()) {
                        if (!contact.isSelect()) {
                            contacts.add(contact);
                        }
                    }
                    mViewModel.updateContact(contacts);
                    dialogFragment.dismiss();
                    resetTopView();
                })
                .build()
                .show(getChildFragmentManager(), Jl_Dialog.class.getCanonicalName());

    }


    private TextView getFooterView() {
        TextView textView = new TextView(requireContext());
        textView.setText(getString(R.string.tip_max_contacts_can_add, MAC_COUNT));
        textView.setGravity(Gravity.CENTER_VERTICAL);
        textView.setTextSize(14);
        textView.setTextColor(getResources().getColor(R.color.text_secondary_disable_color));
        int padding = ValueUtil.dp2px(requireContext(), 14);
        textView.setPadding(padding, padding, padding, padding);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return textView;
    }


    private void tryToRequestContactPermission() {
        showPermissionDialog(Manifest.permission.READ_CONTACTS, (permission ->
                ContactFragmentPermissionsDispatcher.toAddContactFragmentWithPermissionCheck(ContactFragment.this)));
    }

    private static class ContactAdapter extends BaseQuickAdapter<Contact, BaseViewHolder> implements DraggableModule {

        static final int MODE_NORMAL = 0;
        static final int MODE_SORT = 1;
        static final int MODE_DELETE = 2;
        private int mode;//0:普通模式  1:排序模式  2:删除模式

        @SuppressLint("NotifyDataSetChanged")
        public void setMode(int mode) {
            this.mode = mode;
            getDraggableModule().setDragEnabled(mode == MODE_SORT);
            notifyDataSetChanged();
        }

        public int getMode() {
            return mode;
        }

        public ContactAdapter() {
            super(R.layout.item_contact);
        }

        @Override
        protected void convert(@NotNull BaseViewHolder holder, Contact contact) {
            holder.setText(R.id.tv_contact_name, contact.getName());
            holder.setText(R.id.tv_contact_number, contact.getNumber());
            holder.setText(R.id.tv_contact_type, contact.getTypeName());
            holder.setGone(R.id.view_line_contact, getItemPosition(contact) == getData().size());
            holder.setVisible(R.id.iv_contact_end, this.mode != MODE_NORMAL);
            holder.getView(R.id.iv_contact_end).setSelected(contact.isSelect());
            holder.setImageResource(R.id.iv_contact_end, mode == MODE_SORT ? R.drawable.ic_contact_drag_flag : R.drawable.ic_music_manager_chose_selector);
        }


        int getSelectCount() {
            int count = 0;
            for (Contact contact : getData()) {
                if (contact.isSelect()) count++;
            }
            return count;
        }

    }

    private void showContactPermissionDialog(PermissionRequest request) {
        showPermissionDialog(Manifest.permission.READ_CONTACTS, request, null);
    }
}