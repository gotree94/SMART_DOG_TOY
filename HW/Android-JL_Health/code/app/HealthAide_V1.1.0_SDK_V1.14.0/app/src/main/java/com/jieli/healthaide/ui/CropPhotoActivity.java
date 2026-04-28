package com.jieli.healthaide.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;

import com.jieli.component.utils.ValueUtil;
import com.jieli.healthaide.R;
import com.jieli.healthaide.databinding.ActivityCropPhotoBinding;
import com.jieli.healthaide.tool.watch.WatchManager;
import com.jieli.healthaide.ui.base.BaseActivity;
import com.jieli.jl_dialog.Jl_Dialog;
import com.jieli.jl_rcsp.model.WatchConfigure;
import com.jieli.jl_rcsp.model.device.settings.v0.DialExpandInfo;
import com.jieli.jl_rcsp.model.response.ExternalFlashMsgResponse;
import com.jieli.jl_rcsp.util.JL_Log;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropFragment;
import com.yalantis.ucrop.UCropFragmentCallback;
import com.yalantis.ucrop.view.GestureCropImageView;
import com.yalantis.ucrop.view.UCropView;

import java.io.File;

/**
 * 裁剪图片界面
 */
public class CropPhotoActivity extends BaseActivity implements UCropFragmentCallback {

    private ActivityCropPhotoBinding mCropPhotoBinding;
    private UCropFragment mCropFragment;
    private UCropView mUCropView;
    private GestureCropImageView mGestureCropImageView;

    private int cropType;

    private boolean isSaveCropFile;
    private String outputPath;

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    public final static String KEY_CROP_TYPE = "crop_type";
    public final static String KEY_RESOURCE_URI = "resource_uri";
    public final static String KEY_OUTPUT_PATH = "output_path";

    public final static int CROP_TYPE_AVATAR = 1;
    public final static int CROP_TYPE_WATCH_BG = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        mCropPhotoBinding = ActivityCropPhotoBinding.inflate(getLayoutInflater());
        setContentView(mCropPhotoBinding.getRoot());
        setWindowStatus();
        mCropPhotoBinding.tvCropPhotoCancel.setOnClickListener(v -> {
            setResult(Activity.RESULT_CANCELED);
            finish();
        });
        mCropPhotoBinding.tvCropPhotoSelect.setOnClickListener(v -> {
            if (mCropFragment != null && mCropFragment.isAdded()) {
                mCropFragment.cropAndSaveImage();
            }
        });
        mCropPhotoBinding.ivCropPhotoRotate.setOnClickListener(v -> {
            if (mCropFragment != null && mCropFragment.isAdded() && mGestureCropImageView != null) {
                mGestureCropImageView.postRotate(90);
                mGestureCropImageView.setImageToWrapCropBounds();
            }
        });
        if (null == getIntent()) {
            finish();
            return;
        }
        cropType = getIntent().getIntExtra(KEY_CROP_TYPE, 0);
        Uri photoUri = getIntent().getParcelableExtra(KEY_RESOURCE_URI);
        outputPath = getIntent().getStringExtra(KEY_OUTPUT_PATH);
        JL_Log.i(tag, "onCreate", "cropType = " + cropType + ", photoUri = " + photoUri + ", outputPath = " + outputPath);
        if (cropType <= 0 || null == photoUri || null == outputPath) {
            finish();
            return;
        }
        configureUCrop(cropType, photoUri, outputPath);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCropPhotoBinding = null;
        if (!isSaveCropFile && null != outputPath) {
            File file = new File(outputPath);
            file.delete();
        }
    }

    @Override
    public void loadingProgress(boolean showLoader) {

    }

    @Override
    public void onCropFinish(UCropFragment.UCropResult result) {
        JL_Log.i(tag, "onCropFinish", "result : " + result.mResultCode + ", intent = " + result.mResultData);
        isSaveCropFile = result.mResultCode == Activity.RESULT_OK; //是否裁剪成功
        Throwable error = UCrop.getError(result.mResultData);
        if (error != null) {
            JL_Log.e(tag, "onCropFinish", "UCrop error = " + error.getMessage());
            Jl_Dialog tipDialog = Jl_Dialog.builder()
                    .width(0.8f)
                    .cancel(false)
                    .content(getString(R.string.fail_recognize_format))
                    .contentColor(getResources().getColor(R.color.black_242424))
                    .right(getString(R.string.sure))
                    .rightColor(getResources().getColor(R.color.blue_558CFF))
                    .rightClickListener(((view, dialogFragment) -> {
                        dialogFragment.dismiss();
                        finish();
                    }))
                    .build();
            tipDialog.show(getSupportFragmentManager(), "tips_dialog");
            return;
        }
     /*   if (result.mResultCode == Activity.RESULT_OK && cropType == CROP_TYPE_WATCH_BG) { //判断是否需要二次处理图像
            Uri uri = result.mResultData == null ? null : UCrop.getOutput(result.mResultData);
            String photoPath = uri == null ? null : uri.getPath();
            if (null != photoPath) {
                CustomDialManager.getInstance().addCacheThumbImage(photoPath);//保存方图的裁剪图
            }
            final DialExpandInfo dialExpandInfo = getDialExpandInfo();
            if (dialExpandInfo != null) {
                final int shape = dialExpandInfo.getShape();
                if (shape > 0 && shape != DialExpandInfo.SHAPE_RECTANGLE) {
//                    Uri uri = result.mResultData == null ? null : UCrop.getOutput(result.mResultData);
//                    String photoPath = uri == null ? null : uri.getPath();
                    if (null != photoPath) {
                        Bitmap srcBmp = BitmapFactory.decodeFile(photoPath);
                        if (null != srcBmp) {
                            Bitmap destBmp;
                            if (shape == DialExpandInfo.SHAPE_CIRCULAR) {
                                destBmp = BitmapUtil.clipCircleAndFillBitmap(srcBmp, dialExpandInfo.getColor());
                            } else {
                                destBmp = BitmapUtil.clipRoundAndFillBitmap(srcBmp, dialExpandInfo.getRadius(), dialExpandInfo.getColor());
                            }
                            BitmapUtil.bitmapToFile(destBmp, photoPath, 100);
                        }
                    }
                }
            }
        }*/
        setResult(result.mResultCode, result.mResultData);
        finish();
    }

    private ExternalFlashMsgResponse getExternalFlashMsg() {
        final WatchManager watchManager = WatchManager.getInstance();
        return watchManager.getExternalFlashMsg(watchManager.getConnectedDevice());
    }

    private DialExpandInfo getDialExpandInfo() {
        final WatchManager watchManager = WatchManager.getInstance();
        final WatchConfigure watchConfigure = watchManager.getWatchConfigure(watchManager.getConnectedDevice());
        if (null == watchConfigure) return null;
        return watchConfigure.getDialExpandInfo();
    }

    private void configureUCrop(int cropType, Uri photoUri, String outputPath) {
        UCrop.Options options = new UCrop.Options();
        UCrop uCrop = UCrop.of(photoUri, Uri.fromFile(new File(outputPath)));
        options.setCompressionFormat(Bitmap.CompressFormat.PNG);
        options.setCompressionQuality(100);
        //是否隐藏底部控制栏
        options.setHideBottomControls(true);
        //是否可以自由裁剪
        options.setFreeStyleCropEnabled(false);
        //是否显示指示线
        options.setShowCropGrid(false);
        //裁剪区域绘制
        options.setShowCropFrame(true);
        options.setCropFrameStrokeWidth(ValueUtil.dp2px(this, 2));
        if (cropType == CROP_TYPE_WATCH_BG) {
            final ExternalFlashMsgResponse externalFlashMsg = getExternalFlashMsg();
            if (externalFlashMsg == null) {
                finish();
                return;
            }
            int width = 240;
            int height = 280;
            if (externalFlashMsg.getScreenWidth() > 0) {
                width = externalFlashMsg.getScreenWidth();
            }
            if (externalFlashMsg.getScreenHeight() > 0) {
                height = externalFlashMsg.getScreenHeight();
            }
            DialExpandInfo dialExpandInfo = getDialExpandInfo();
            boolean isCircular = dialExpandInfo != null && dialExpandInfo.isCircular();
            options.setCircleDimmedLayer(isCircular);
            options.withAspectRatio(width, height);
            if (dialExpandInfo != null && dialExpandInfo.getShape() == DialExpandInfo.SHAPE_ROUNDED_RECTANGLE
                    && dialExpandInfo.getRadius() > 0) {
                options.setCropFrameRadius(ValueUtil.dp2px(this, dialExpandInfo.getRadius()));
            }
            JL_Log.i(tag, "configureUCrop", "width = " + width + ", height = " + height + ", isCircular = " + isCircular + ", " + dialExpandInfo);
            uCrop.withAspectRatio(width, height)
                    .withMaxResultSize(width, height);
        } else {
            options.setCircleDimmedLayer(true);
            uCrop.withAspectRatio(1.0f, 1.0f)
                    .withMaxResultSize(180, 180);
        }

        //设置图像最大体积
        options.setMaxBitmapSize(300 * 1024);
        uCrop.withOptions(options);
        setupFragment(uCrop);
    }

    private void setupFragment(UCrop uCrop) {
        mCropFragment = uCrop.getFragment(uCrop.getIntent(CropPhotoActivity.this).getExtras());
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fl_crop_photo_container, mCropFragment, UCropFragment.TAG)
                .commitAllowingStateLoss();

        mHandler.postDelayed(() -> {
            if (mCropFragment != null && mCropFragment.isAdded()) {
                mUCropView = mCropFragment.requireView().findViewById(com.yalantis.ucrop.R.id.ucrop);
                mGestureCropImageView = mUCropView.getCropImageView();
            }
        }, 200);

    }
}