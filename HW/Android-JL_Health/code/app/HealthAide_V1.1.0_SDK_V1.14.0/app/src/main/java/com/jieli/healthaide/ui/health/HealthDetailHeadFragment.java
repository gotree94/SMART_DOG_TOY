package com.jieli.healthaide.ui.health;

import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.jieli.healthaide.databinding.LayoutHealthDetailHeadBinding;

public abstract class HealthDetailHeadFragment extends Fragment {
    LayoutHealthDetailHeadBinding mLayoutHealthDetailHeadBinding;
    protected final int CALENDAR_TYPE_DAY = 1;
    protected final int CALENDAR_TYPE_WEEK = 2;
    protected final int CALENDAR_TYPE_MONTH = 3;
    protected final int CALENDAR_TYPE_YEAR = 4;
    private int mCurrentCalendarType = CALENDAR_TYPE_DAY;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        initView();
    }

    private void initView() {
        if (mLayoutHealthDetailHeadBinding == null) return;
        mLayoutHealthDetailHeadBinding.tvTitle.setText(getFragmentTitle());
        mLayoutHealthDetailHeadBinding.ibtCalender.setOnClickListener(view -> calendarSelect());
        mLayoutHealthDetailHeadBinding.ibtBack.setOnClickListener(view -> getActivity().finish());
        mLayoutHealthDetailHeadBinding.tabCalendar.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switchCalendarType(tab.getPosition() + 1);
                mLayoutHealthDetailHeadBinding.ibtCalender.setVisibility(tab.getPosition() + 1 > CALENDAR_TYPE_WEEK ? View. GONE: View.VISIBLE);
                mCurrentCalendarType = tab.getPosition() + 1;
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    protected abstract String getFragmentTitle();

    protected abstract void switchCalendarType(int calendarType);

    protected abstract void calendarSelect();
}