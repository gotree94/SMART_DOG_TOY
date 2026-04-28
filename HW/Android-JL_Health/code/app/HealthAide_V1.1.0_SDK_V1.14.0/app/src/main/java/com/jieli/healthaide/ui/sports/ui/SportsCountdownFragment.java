package com.jieli.healthaide.ui.sports.ui;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.jieli.component.ActivityManager;
import com.jieli.healthaide.R;
import com.jieli.healthaide.data.entity.SportRecord;
import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.healthaide.ui.ContentActivity;
import com.jieli.healthaide.ui.base.BaseFragment;
import com.jieli.jl_rcsp.impl.HealthOpImpl;
import com.jieli.jl_rcsp.interfaces.OnOperationCallback;
import com.jieli.jl_rcsp.model.base.BaseError;
import com.jieli.jl_rcsp.util.JL_Log;

import java.util.Objects;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 4/6/21
 * @desc :
 */
public class SportsCountdownFragment extends BaseFragment {

    private ValueAnimator valueAnimator;


    public static void startByOutdoorRunning(Context context) {
        Bundle bundle = new Bundle();
        bundle.putInt(RunningParentFragment.KEY_RUNNING_TYPE, SportRecord.TYPE_OUTDOOR);
        ContentActivity.startContentActivity(context, SportsCountdownFragment.class.getCanonicalName(), bundle);
    }

    public static void startByIndoorRunning(Context context) {
        Bundle bundle = new Bundle();
        bundle.putInt(RunningParentFragment.KEY_RUNNING_TYPE, SportRecord.TYPE_INDOOR);
        ContentActivity.startContentActivity(context, SportsCountdownFragment.class.getCanonicalName(), bundle);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Objects.requireNonNull(container).setBackgroundColor(getResources().getColor(R.color.main_color));

        ImageView imageView = new ImageView(requireContext());
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        imageView.setLayoutParams(params);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        final int[] res = new int[]{
                R.drawable.nub_3,
                R.drawable.nub_2,
                R.drawable.nub_1,
                R.drawable.nub_0,
        };
        int[] audios = new int[]{
                R.raw.num_3,
                R.raw.num_2,
                R.raw.num_1,
                R.raw.go
        };
        int[] ids = new int[4];
        final SoundPool soundPool;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(4)
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setLegacyStreamType(AudioManager.STREAM_RING)
                            .build())
                    .build();
        } else {
            soundPool = new SoundPool(4, AudioManager.STREAM_RING, 0);
        }
        for (int i = 0; i < audios.length; i++) {
            ids[i] = soundPool.load(getContext(), audios[i], 1);
        }

        valueAnimator = ValueAnimator.ofFloat(0, 3.99f);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.setDuration(4000);
        InnerAnimationListener listener = new InnerAnimationListener() {
            private int index = -1;

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int tmp = (int) ((float) animation.getAnimatedValue());
                if (tmp != index) {
                    index = tmp;
                    imageView.setImageResource(res[index]);
                    soundPool.play(ids[index], 1.0F, 1.0F, 1, 0, 1.0F);
                }
                float scale = (float) (animation.getAnimatedValue()) - index;
                imageView.setScaleX(scale);
                imageView.setScaleY(scale);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                JL_Log.i(tag, "onAnimationEnd", "index = " + index);
                soundPool.release();
                if (index >= res.length - 1) {
                    doAfterAnimation();
                }
            }
        };


        valueAnimator.addUpdateListener(listener);
        valueAnimator.addListener(listener);
        soundPool.setOnLoadCompleteListener((soundPool1, sampleId, status) -> {
            if (valueAnimator.isStarted()) return;
            valueAnimator.start();
        });
        return imageView;
    }

    @Override
    public void onPause() {
        super.onPause();
        valueAnimator.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        valueAnimator.resume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        valueAnimator.cancel();
    }

    private void doAfterAnimation() {
        if (isDetached() || !isAdded()) return;
        final Bundle args = getArguments();
        int sportMode = args == null ? 0 : args.getInt(RunningParentFragment.KEY_RUNNING_TYPE, 0);
        HealthOpImpl healthOp = WatchManager.getInstance().getHealthOp();
        JL_Log.d(tag, "doAfterAnimation", "startSports");
        healthOp.startSports(healthOp.getConnectedDevice(), sportMode, new OnOperationCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                JL_Log.i(tag, "onSuccess", "startSports success!!!");
                //进入不同的跑步界面
                Activity activity;
                if (!isDetached() && isAdded()) {
                    activity = requireActivity();
                } else {
                    activity = ActivityManager.getInstance().getTopActivity();
                }
                if (activity != null && !activity.isDestroyed()) {
                    ContentActivity.startContentActivity(activity, RunningParentFragment.class.getCanonicalName(), args);
                    if (activity instanceof ContentActivity) {
                        activity.finish();
                    }
                }
            }

            @Override
            public void onFailed(BaseError error) {
                JL_Log.w(tag, "onFailed", "发送开始运动命令失败:" + error);
                showTips(R.string.start_sports_failed);
                if (!requireActivity().isDestroyed()) {
                    requireActivity().finish();
                }
            }
        });
    }


    private static class InnerAnimationListener implements ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {
        private long time;

        @Override
        public void onAnimationStart(Animator animation) {
            time = System.currentTimeMillis();
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            JL_Log.d("SportsCountdownFragment", "onAnimationEnd", "sportsCountdown take time = " + (System.currentTimeMillis() - time));

        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {

        }
    }

}
