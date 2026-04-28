package com.jieli.healthaide.ui.health;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.FragmentSleepBinding;
import com.jieli.healthaide.ui.health.sleep.SleepDataFragment;
import com.jieli.healthaide.ui.health.sleep.SleepDayFragment;
import com.jieli.healthaide.ui.health.sleep.SleepMonthFragment;
import com.jieli.healthaide.ui.health.sleep.SleepWeekFragment;
import com.jieli.healthaide.ui.health.sleep.SleepYearFragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SleepFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SleepFragment extends HealthDetailHeadFragment {
    private FragmentSleepBinding fragmentSleepBinding;

    public SleepFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static SleepFragment newInstance() {
        SleepFragment fragment = new SleepFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentSleepBinding = FragmentSleepBinding.inflate(inflater, container, false);
        mLayoutHealthDetailHeadBinding = fragmentSleepBinding.layoutHealthHead;
        mLayoutHealthDetailHeadBinding.tabCalendar.setTabTextColors(getResources().getColor(R.color.white_light_b3), getResources().getColor(R.color.blue_4852CA));
        fragmentSleepBinding.vp2Sleep.setAdapter(new FragmentAdapter(requireActivity()));
        fragmentSleepBinding.vp2Sleep.setOffscreenPageLimit(4);
        fragmentSleepBinding.vp2Sleep.setUserInputEnabled(false);

        return fragmentSleepBinding.getRoot();
    }

    @Override
    protected String getFragmentTitle() {
        return getResources().getString(R.string.sleep);
    }

    @Override
    protected void switchCalendarType(int calendarType) {
        //todo 根据时间类型切换显示数据
        fragmentSleepBinding.vp2Sleep.setCurrentItem(calendarType - 1,false);

        switch (calendarType) {
            case CALENDAR_TYPE_DAY:
                break;
            case CALENDAR_TYPE_WEEK:
                break;
            case CALENDAR_TYPE_MONTH:
                break;
            case CALENDAR_TYPE_YEAR:
                break;
        }
    }

    @Override
    protected void calendarSelect() {
        int currentItem = fragmentSleepBinding.vp2Sleep.getCurrentItem();
        FragmentAdapter adapter= (FragmentAdapter) fragmentSleepBinding.vp2Sleep.getAdapter();
        SleepDataFragment sleepDataFragment = (SleepDataFragment) adapter.fragments[currentItem];
        sleepDataFragment.calendarSelect();
    }

    private static class FragmentAdapter extends FragmentStateAdapter {

         Fragment[] fragments;

        public FragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
            fragments = new Fragment[]{
                    SleepDayFragment.newInstance(),
                    SleepWeekFragment.newInstance(),
                    SleepMonthFragment.newInstance(),
                    SleepYearFragment.newInstance()
            };

        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return fragments[position];
        }

        @Override
        public int getItemCount() {
            return fragments.length;
        }
    }
}
