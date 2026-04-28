package com.jieli.healthaide.ui.mine;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentMineBinding;
import com.jieli.healthaide.ui.ContentActivity;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.ui.mine.about.AboutFragment;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 我的界面
 */
public class MineFragment extends BaseFragment {
    private FragmentMineBinding mineBinding;
    private UserInfoViewModel userInfoViewModel;
    private ImageView ivAvatar;
    private TextView tvNickname;


    public MineFragment() {
        // Required empty public constructor
    }

    public static MineFragment newInstance() {
        return new MineFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mineBinding = FragmentMineBinding.inflate(inflater, container, false);
        mineBinding.rvMine.setLayoutManager(new LinearLayoutManager(requireContext()));
        ItemAdapter itemAdapter = new ItemAdapter();
        String[] names = getResources().getStringArray(R.array.mine_item_names);
        TypedArray icons = getResources().obtainTypedArray(R.array.mine_item_icons);
        List<Item> list = new ArrayList<>();
        for (int i = 0; i < names.length-1 && i < icons.length(); i++) {
            list.add(new Item(icons.getResourceId(i, 0), names[i]));
        }
        icons.recycle();
        itemAdapter.setList(list);

        View header = inflater.inflate(R.layout.item_mine_header, null);
        ivAvatar = header.findViewById(R.id.iv_mine_item_left);
        tvNickname = header.findViewById(R.id.tv_mine_item_name);
        ivAvatar.setImageResource(R.drawable.ic_userinfo_avatar_man);

        header.setOnClickListener(v -> ContentActivity.startContentActivity(requireContext(), UserInfoFragment.class.getCanonicalName()));
        itemAdapter.setHeaderView(header);
        mineBinding.rvMine.setAdapter(itemAdapter);

        itemAdapter.setOnItemClickListener((adapter, view, position) -> {
            switch (position) {
                case 0: //我的数据
                    ContentActivity.startContentActivity(requireContext(), MyDataFragment.class.getCanonicalName());
                    break;
                case 1: //运动周报
                    ContentActivity.startContentActivity(requireContext(), SportsWeeklyFragment.class.getCanonicalName());
                    break;
                case 2: //设置
                    ContentActivity.startContentActivity(requireContext(), SettingFragment.class.getCanonicalName());
                    break;
                case 3: //关于
                    ContentActivity.startContentActivity(requireContext(), AboutFragment.class.getCanonicalName());
//                    startActivity(new Intent(getContext(), NFCActivity.class));
//                    ContentActivity.startContentActivity(requireContext(), NFCCardBagFragment.class.getCanonicalName());
//
//                    ContentActivity.startContentActivity(requireContext(), ContactFragment.class.getCanonicalName());
                    break;
                case 4:
//                    ContentActivity.startContentActivity(requireContext(), PhoneRegionChoseFragment.class.getCanonicalName());
                    ContentActivity.startContentActivity(requireContext(), AboutFragment.class.getCanonicalName());
                    break;
//                case 5:
//                    SyncTaskManager.getInstance().addTask(new WeatherSyncTask(SyncTaskManager.getInstance(),SyncTaskManager.getInstance()));
//
//                    break;
            }

        });


        return mineBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        userInfoViewModel = new ViewModelProvider(this).get(UserInfoViewModel.class);
        userInfoViewModel.userInfoLiveData.observe(getViewLifecycleOwner(), data -> {
            if (TextUtils.isEmpty(data.getAvatarUrl())) {
                int avatarRes = R.drawable.ic_userinfo_avatar_man;
                if (data.getGender() == 1) {
                    avatarRes = R.drawable.ic_userinfo_avatar_woman;
                }
                ivAvatar.setImageResource(avatarRes);
            } else {
                Glide.with(this).load(data.getAvatarUrl()).into(ivAvatar);
            }

            String nickname = data.getNickname();
            if (TextUtils.isEmpty(nickname)) {
                nickname = getString(R.string.no_nickname);
            }
            tvNickname.setText(nickname);


        });
    }

    @Override
    public void onResume() {
        super.onResume();
        userInfoViewModel.getUserInfo();
    }

    private static class ItemAdapter extends BaseQuickAdapter<Item, BaseViewHolder> {

        public ItemAdapter() {
            super(R.layout.item_mine);
        }

        @Override
        protected void convert(@NotNull BaseViewHolder baseViewHolder, Item item) {
            baseViewHolder.setImageResource(R.id.iv_mine_item_left, item.resId);
            baseViewHolder.setText(R.id.tv_mine_item_name, item.name);
        }
    }


    private static class Item {
        public int resId;
        public String name;

        public Item(int resId, String name) {
            this.resId = resId;
            this.name = name;
        }
    }
}