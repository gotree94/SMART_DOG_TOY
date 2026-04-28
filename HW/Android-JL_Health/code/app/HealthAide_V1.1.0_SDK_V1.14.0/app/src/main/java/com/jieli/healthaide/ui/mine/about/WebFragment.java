package com.jieli.healthaide.ui.mine.about;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jieli.healthaide.databinding.FragmentWebBinding;
import com.jieli.healthaide.tool.net.NetWorkStateModel;
import com.jieli.healthaide.tool.net.NetworkStateHelper;
import com.jieli.healthaide.ui.ContentActivity;
import com.jieli.healthaide.ui.base.BaseFragment;


/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/11/21 9:19 AM
 * @desc :
 */
public final class WebFragment extends BaseFragment implements NetworkStateHelper.Listener {

    final static String KEY_URL = "url";
    final static String KEY_TITLE = "title";

    FragmentWebBinding binding;


    public static void start(Context context, String title, String url) {
        Bundle bundle = new Bundle();
        bundle.putString(WebFragment.KEY_TITLE, title);
        bundle.putString(WebFragment.KEY_URL, url);
        ContentActivity.startContentActivity(context, WebFragment.class.getCanonicalName(), bundle);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentWebBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle == null) {
            requireActivity().finish();
            return;
        }
        final String url = bundle.getString(KEY_URL);
        if (url == null || url.isEmpty()) {
            requireActivity().finish();
            return;
        }
        final String title = bundle.getString(KEY_TITLE);

        binding.layoutTopbar.tvTopbarLeft.setOnClickListener(v -> requireActivity().onBackPressed());
        binding.layoutTopbar.tvTopbarTitle.setText(title);
        authorization();
        binding.webAbout.loadUrl(url);
    }

    @Override
    public void onNetworkStateChange(NetWorkStateModel model) {


    }

    @SuppressLint("SetJavaScriptEnabled")
    private void authorization() {
        WebSettings settings = binding.webAbout.getSettings();
        settings.setDefaultTextEncodingName("UTF-8");
        settings.setUserAgentString("User-Agent:Android");
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setSupportZoom(true);
        settings.setUseWideViewPort(true);
    }
}
