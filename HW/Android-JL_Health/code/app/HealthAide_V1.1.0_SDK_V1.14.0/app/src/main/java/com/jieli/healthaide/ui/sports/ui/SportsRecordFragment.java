package com.jieli.healthaide.ui.sports.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.module.LoadMoreModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.jieli.healthaide.R;
import com.jieli.healthaide.data.entity.SportRecord;
import com.jieli.healthaide.databinding.FragmentSportRecordBinding;
import com.jieli.healthaide.tool.unit.BaseUnitConverter;
import com.jieli.healthaide.tool.unit.Converter;
import com.jieli.healthaide.tool.unit.KMUnitConverter;
import com.jieli.healthaide.ui.ContentActivity;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.healthaide.ui.sports.model.SportsRecordAndLocation;
import com.jieli.healthaide.ui.sports.viewmodel.SportRecordViewModel;
import com.jieli.healthaide.ui.widget.CustomLoadMoreView;
import com.jieli.healthaide.util.CalendarUtil;
import com.jieli.healthaide.util.CustomTimeFormatUtil;
import com.jieli.healthaide.util.FormatUtil;
import com.jieli.jl_rcsp.util.JL_Log;

import org.jetbrains.annotations.NotNull;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 4/8/21
 * @desc :
 */
public class SportsRecordFragment extends BaseFragment {
    private SportRecordViewModel mViewModel;
    private Adapter mAdapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        com.jieli.healthaide.databinding.FragmentSportRecordBinding mBinding = FragmentSportRecordBinding.bind(inflater.inflate(R.layout.fragment_sport_record, container, false));
        mBinding.viewTopbar.tvTopbarTitle.setText(R.string.movement_record);
        mBinding.viewTopbar.tvTopbarLeft.setOnClickListener(v -> requireActivity().onBackPressed());
        mAdapter = new Adapter();
        mBinding.rvSportRecord.setAdapter(mAdapter);
        mBinding.rvSportRecord.setLayoutManager(new LinearLayoutManager(requireContext()));
        mAdapter.setOnItemClickListener((adapter, view, position) -> {
            Bundle bundle = new Bundle();
            SportRecord sportRecord = mAdapter.getItem(position).getSportRecord();
            bundle.putLong(RunningDetailFragment.KEY_RECORD_START_TIME, sportRecord.getStartTime());
            switch (sportRecord.getType()) {
                case SportRecord.TYPE_OUTDOOR:
                    ContentActivity.startContentActivity(requireContext(), RunningDetailWithMapFragment.class.getCanonicalName(), bundle);
                    break;
                case SportRecord.TYPE_INDOOR:
                    ContentActivity.startContentActivity(requireContext(), RunningDetailFragment.class.getCanonicalName(), bundle);
                    break;
                default:
                    //ToastUtil.showToastShort("不支持的运动类型");
                    break;
            }

        });
        mAdapter.getLoadMoreModule().setLoadMoreView(new CustomLoadMoreView());
        mAdapter.getLoadMoreModule().setOnLoadMoreListener(() -> {
            JL_Log.d(tag, "OnLoadMore", "load more sports");
            mViewModel.queryByPage(SportRecordViewModel.PAGE_SIZE, mAdapter.getData().size());
        });
        mAdapter.getLoadMoreModule().setEnableLoadMore(true);
        mAdapter.getLoadMoreModule().setAutoLoadMore(true);
        mAdapter.getLoadMoreModule().setEnableLoadMoreIfNotFullPage(true);
        mAdapter.setEmptyView(R.layout.empty_sports_record);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        JL_Log.d(tag, "onActivityCreated", "");
        mViewModel = new ViewModelProvider(requireActivity()).get(SportRecordViewModel.class);
        mViewModel.getRecordsLiveData().observe(getViewLifecycleOwner(), sportRecords -> {
            if (sportRecords == null || sportRecords.size() < SportRecordViewModel.PAGE_SIZE) {
                mAdapter.getLoadMoreModule().loadMoreEnd();
            }
            if (sportRecords != null) {
                mAdapter.addData(sportRecords);
            }
        });
        mViewModel.queryByPage(SportRecordViewModel.PAGE_SIZE, mAdapter.getData().size());
    }


    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    private static class Adapter extends BaseQuickAdapter<SportsRecordAndLocation, BaseViewHolder> implements LoadMoreModule {

        final Converter unitConverter = new KMUnitConverter().getConverter(BaseUnitConverter.getType());


        public Adapter() {
            super(R.layout.item_sport_record);
        }


        @SuppressLint({"SimpleDateFormat"})
        @Override
        protected void convert(@NotNull BaseViewHolder holder, SportsRecordAndLocation item) {

            SportRecord sportRecord = item.getSportRecord();
            int[] typeStrings = new int[]{R.string.sport_other, R.string.outdoor, R.string.indoor};
            int type = Math.min(sportRecord.getType(), 2);
            holder.setText(R.id.tv_sport_record_date, CustomTimeFormatUtil.dateFormat("yyyy/MM/dd\t\tHH:mm").format(sportRecord.getStartTime()));
            holder.setText(R.id.tv_sport_record_type, typeStrings[type]);

            holder.setText(R.id.tv_sport_record_distance, CalendarUtil.formatString("%.2f", unitConverter.value(sportRecord.getDistance() / 1000f)));
            holder.setText(R.id.tv_sport_record_distance_unit, unitConverter.unit());
            holder.setText(R.id.tv_sport_record_take_time, CalendarUtil.formatSeconds(sportRecord.getDuration()));
            double pace = sportRecord.getDistance() <= 0 ? 0 : (1.0 * sportRecord.getDuration() / (sportRecord.getDistance() / 1000.0));
            holder.setText(R.id.tv_sport_record_pace, FormatUtil.paceFormat((long) pace));
            holder.setText(R.id.tv_sport_record_kcal, CalendarUtil.formatString("%d", sportRecord.getKcal()));
            holder.setImageResource(R.id.iv_sports_record_map, sportRecord.getType() == SportRecord.TYPE_OUTDOOR ? R.drawable.record_icon_outdoor_nol : R.drawable.record_icon_indoor_nol);
//            todo 使用轨迹绘图
//            ImageView imageView = holder.getView(R.id.iv_sports_record_map);
//            ImageView imageView = holder.getView(R.id.iv_sports_record_map);
//            if (!TextUtils.isEmpty(item.getBmpPath())) {
//                JL_Log.e("sen","path--->"+item.getBmpPath());
//                Glide.with(imageView)
//                        .asBitmap()
//                        .load(new File(item.getBmpPath()))
//                        .into(imageView);
//            } else {
//                holder.setImageResource(R.id.iv_sports_record_map, R.mipmap.ic_logo);
//            }
        }

    }
}
