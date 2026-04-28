package com.jieli.healthaide.ui.health;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentHealthDetailHeadBinding;
import com.jieli.healthaide.ui.health.weight.WeightDataFragment;
import com.jieli.healthaide.ui.health.weight.WeightDayFragment;
import com.jieli.healthaide.ui.health.weight.WeightMonthFragment;
import com.jieli.healthaide.ui.health.weight.WeightWeekFragment;
import com.jieli.healthaide.ui.health.weight.WeightYearFragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WeightFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WeightFragment extends HealthDetailHeadFragment {

    private FragmentHealthDetailHeadBinding fragmentHealthDetailHeadBinding;

    public WeightFragment() {
        // Required empty public constructor
    }

    private final Fragment[] mFragments = new Fragment[]{
            WeightDayFragment.newInstance(),
            WeightWeekFragment.newInstance(),
            WeightMonthFragment.newInstance(),
            WeightYearFragment.newInstance()
    };

    // TODO: Rename and change types and number of parameters
    public static StepFragment newInstance() {
        StepFragment fragment = new StepFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentHealthDetailHeadBinding = FragmentHealthDetailHeadBinding.inflate(inflater, container, false);
        mLayoutHealthDetailHeadBinding = fragmentHealthDetailHeadBinding.layoutHealthHead;
        mLayoutHealthDetailHeadBinding.tabCalendar.setTabTextColors(getResources().getColor(R.color.white_light_b3), getResources().getColor(R.color.blue_4852CA));
        fragmentHealthDetailHeadBinding.clHealthHeaderMain.setBackground(getContext().getDrawable(R.drawable.bg_weight_fragment));
        initView();
        return fragmentHealthDetailHeadBinding.getRoot();
    }

    private void initView() {
        fragmentHealthDetailHeadBinding.vpHealthDetail.setOffscreenPageLimit(4);
        fragmentHealthDetailHeadBinding.vpHealthDetail.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return mFragments[position];
            }

            @Override
            public int getItemCount() {
                return mFragments.length;
            }
        });
        fragmentHealthDetailHeadBinding.vpHealthDetail.setUserInputEnabled(false);
    }

    @Override
    protected String getFragmentTitle() {
        return getResources().getString(R.string.weight);
    }

    @Override
    protected void switchCalendarType(int calendarType) {
        //todo 根据时间类型切换显示数据
        switch (calendarType) {
            case CALENDAR_TYPE_DAY:
                fragmentHealthDetailHeadBinding.vpHealthDetail.setCurrentItem(0, false);
                break;
            case CALENDAR_TYPE_WEEK:
                fragmentHealthDetailHeadBinding.vpHealthDetail.setCurrentItem(1, false);
                break;
            case CALENDAR_TYPE_MONTH:
                fragmentHealthDetailHeadBinding.vpHealthDetail.setCurrentItem(2, false);
                break;
            case CALENDAR_TYPE_YEAR:
                fragmentHealthDetailHeadBinding.vpHealthDetail.setCurrentItem(3, false);
                break;
        }
    }

    @Override
    protected void calendarSelect() {
        int currentItem = fragmentHealthDetailHeadBinding.vpHealthDetail.getCurrentItem();
        WeightDataFragment weightDataFragment = (WeightDataFragment) mFragments[currentItem];
        weightDataFragment.calendarSelect();
    }

}