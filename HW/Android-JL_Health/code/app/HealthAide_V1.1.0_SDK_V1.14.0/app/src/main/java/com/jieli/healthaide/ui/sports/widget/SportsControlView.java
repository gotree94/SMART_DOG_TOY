package com.jieli.healthaide.ui.sports.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.transition.AutoTransition;
import androidx.transition.ChangeBounds;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import com.jieli.component.utils.ValueUtil;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.LayoutSportsControllBinding;
import com.jieli.healthaide.util.UIHelper;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/11/15
 * @desc :
 */
public class SportsControlView extends ConstraintLayout {
    private LayoutSportsControllBinding binding;

    private ConstraintSet resetConstraintSet;
    private OnEventListener onEventListener;

    // 添加防抖保护
    private boolean isAnimating = false;
    private final int margin108;
    private final int margin8;

    public SportsControlView(Context context) {
        this(context, null);
    }

    public SportsControlView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SportsControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }


    public SportsControlView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        margin108 = ValueUtil.dp2px(context, 108);
        margin8 = ValueUtil.dp2px(context, 8);
        init(context);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getContext().registerReceiver(broadcastReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
    }

    @Override
    protected void onDetachedFromWindow() {
        getContext().unregisterReceiver(broadcastReceiver);
        super.onDetachedFromWindow();
    }

    /**
     * 设置事件回调
     *
     * @param onEventListener
     */
    public void setOnEventListener(OnEventListener onEventListener) {
        this.onEventListener = onEventListener;
    }

    /**
     * 设置是否显示地图按钮
     *
     * @param show
     */
    public void showMapBtn(boolean show) {
        binding.ivMap.setVisibility(show ? VISIBLE : INVISIBLE);
    }

    /**
     * 恢复运动
     */
    public void resume() {
        if (resetConstraintSet == null || isAnimating) return;
        isAnimating = true;
        TransitionManager.beginDelayedTransition(SportsControlView.this,
                new AutoTransition().addListener(new Transition.TransitionListener() {
                    @Override
                    public void onTransitionStart(@NonNull Transition transition) {

                    }

                    @Override
                    public void onTransitionEnd(@NonNull Transition transition) {
                        isAnimating = false;
                    }

                    @Override
                    public void onTransitionCancel(@NonNull Transition transition) {
                        isAnimating = false;
                    }

                    @Override
                    public void onTransitionPause(@NonNull Transition transition) {

                    }

                    @Override
                    public void onTransitionResume(@NonNull Transition transition) {

                    }
                }));
        resetConstraintSet.applyTo(SportsControlView.this);
    }

    /**
     * 暂停运动
     */
    public void pause() {
        if (isAnimating) return;
        isAnimating = true;
        ConstraintSet constraintSet = new ConstraintSet();
        if (resetConstraintSet == null) {
            resetConstraintSet = new ConstraintSet();
            resetConstraintSet.clone(SportsControlView.this);
        }
        constraintSet.clone(SportsControlView.this);

        constraintSet.setVisibility(R.id.iv_stop, View.INVISIBLE);
        constraintSet.setMargin(R.id.iv_lock, ConstraintSet.END, margin108);
        constraintSet.setMargin(R.id.iv_map, ConstraintSet.START, margin108);
        constraintSet.clear(R.id.iv_continue, ConstraintSet.START);
        constraintSet.connect(R.id.iv_continue, ConstraintSet.END, R.id.view_center, ConstraintSet.START, margin8);
        constraintSet.clear(R.id.iv_end, ConstraintSet.END);
        constraintSet.connect(R.id.iv_end, ConstraintSet.START, R.id.view_center, ConstraintSet.END, margin8);

        final Transition.TransitionListener listener = new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(@NonNull Transition transition) {

            }

            @Override
            public void onTransitionEnd(@NonNull Transition transition) {
                // 确保最终状态正确
                UIHelper.hide(binding.ivStop);
                isAnimating = false;
            }

            @Override
            public void onTransitionCancel(@NonNull Transition transition) {
                // 即使取消，也要保证状态
                UIHelper.hide(binding.ivStop);
                isAnimating = false;
            }

            @Override
            public void onTransitionPause(@NonNull Transition transition) {

            }

            @Override
            public void onTransitionResume(@NonNull Transition transition) {

            }
        };

        ChangeBounds transition = new ChangeBounds();
        transition.setDuration(200).addListener(listener);

        TransitionManager.beginDelayedTransition(SportsControlView.this, transition);
        constraintSet.applyTo(SportsControlView.this);
    }

    private void init(Context context){
        View view = LayoutInflater.from(context).inflate(R.layout.layout_sports_controll, this, true);
        binding = LayoutSportsControllBinding.bind(view);

        binding.ivContinue.setOnClickListener(v -> {
            resume();
            if (onEventListener != null) onEventListener.onResume();
        });


        binding.ivLock.setOnClickListener(v -> {
            binding.clSliderLock.setVisibility(binding.clSliderLock.getVisibility() == VISIBLE ? GONE : VISIBLE);
            if (onEventListener != null)
                onEventListener.onLock(binding.clSliderLock.getVisibility() == GONE);
        });
        binding.ivMap.setOnClickListener(v -> {
            if (onEventListener != null) onEventListener.onMap();
        });

        binding.ivStop.setOnClickListener(v -> {
            pause();
            if (onEventListener != null) onEventListener.onPause();
        });
        binding.ivEnd.setOnPressProgressListener((progress, end) -> {
            if (progress == 100 && end) {
                binding.tvStopTip.setVisibility(View.INVISIBLE);
                if (onEventListener != null) onEventListener.onStop();
            } else if (progress == 0) {
                binding.tvStopTip.setVisibility(View.VISIBLE);
            } else if (end) {
                binding.tvStopTip.setVisibility(View.INVISIBLE);
            }
        });
        binding.viewLock.setOnTouchListener(new View.OnTouchListener() {
            private float downX;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        downX = event.getX();
                        break;
                    case MotionEvent.ACTION_MOVE: {
                        float dx = Math.abs(event.getX() - downX);
                        float alpha = 1 - dx / v.getWidth();
                        v.setAlpha(alpha);
                    }
                    break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        float dx = Math.abs(event.getX() - downX);
                        v.setAlpha(1);
                        if (dx > v.getWidth() / 3f) {
                            binding.clSliderLock.setVisibility(View.GONE);
                            if (onEventListener != null)
                                onEventListener.onLock(binding.clSliderLock.getVisibility() == GONE);
                        }
                        break;
                }
                return true;
            }
        });
    }


    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) return;
            if (!TextUtils.isEmpty(action)) {
                switch (action) {
                    case Intent.ACTION_SCREEN_OFF:
                        binding.clSliderLock.setVisibility(View.VISIBLE);
                        onEventListener.onLock(binding.clSliderLock.getVisibility() == GONE);
                        break;
                }
            }
        }
    };

    public static interface OnEventListener {
        void onResume();

        void onStop();

        void onPause();

        void onLock(boolean lock);

        void onMap();
    }
}
