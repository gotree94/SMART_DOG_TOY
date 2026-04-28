package com.jieli.healthaide.ui.sports.ui.set;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.ItemSportPermissionBinding;

import org.jetbrains.annotations.NotNull;

/**
 * @ClassName: SportPermissionAdapter
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/10/27 9:29
 */
public class SportPermissionAdapter extends BaseQuickAdapter<SportPermission, BaseDataBindingHolder<ItemSportPermissionBinding>> {
    public SportPermissionAdapter() {
        super(R.layout.item_sport_permission);
    }

    @Override
    protected void convert(@NotNull BaseDataBindingHolder<ItemSportPermissionBinding> itemSportPermissionBindingBaseDataBindingHolder, SportPermission sportPermission) {
        ItemSportPermissionBinding binding = itemSportPermissionBindingBaseDataBindingHolder.getDataBinding();
        assert binding != null;
        binding.tvPermissionTitle.setText(sportPermission.permissionTitle);
        binding.tvPermissionDescribe.setText(sportPermission.permissionDescribe);
        binding.tvPermissionOperation.setText(sportPermission.permissionOperate);
        binding.tvPermissionOperation.setOnClickListener(v -> sportPermission.operate());
    }
}
