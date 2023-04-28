package com.bird.yy.wifiproject.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.bird.yy.wifiproject.R;
import com.bird.yy.wifiproject.entity.AdBean;
import com.bird.yy.wifiproject.manager.AdManage;
import com.bird.yy.wifiproject.utils.Constant;

public class PwdDialog extends Dialog {
    private ConnectWifiListener connectWifi;
    private String wifiSSID;
    private String pwd;
    private FragmentActivity activity;
    private FrameLayout frameLayout;

    public PwdDialog(@NonNull Context context, String wifiSSID, String pwd, FragmentActivity activity) {
        super(context);
        this.wifiSSID = wifiSSID;
        this.pwd = pwd;
        this.activity = activity;
    }

    public void setConnectWifi(ConnectWifiListener connectWifi) {
        this.connectWifi = connectWifi;
    }

    private void loadNativeAd() {
        Log.d("xxxxxx","load");
        AdBean adBean = Constant.Companion.getAdMap().get(Constant.adNative_wifi_p);
        if (adBean==null){
            loadAd();
        }else {
            long time = System.currentTimeMillis()-adBean.getSaveTime();
            if (time>Constant.timeOut||adBean.getAd()==null){
                loadAd();
            }else {
                showNativeAd(adBean);
            }
        }
    }
    private void loadAd(){
        new AdManage().loadAd(Constant.adNative_wifi_p,
                getContext(), new AdManage.OnLoadAdCompleteListener() {
                    @Override
                    public void onLoadAdComplete(@Nullable AdBean ad) {
                        if (ad != null && ad.getAd() != null) {
                            showNativeAd(ad);
                        }
                    }

                    @Override
                    public void isMax() {

                    }
                }
        );
    }
    private void showNativeAd(AdBean adBean) {
        Log.d("xxxxxx","load3333");
        if (frameLayout != null) {
            Log.d("xxxxxx","load4444");
            new AdManage().showAd(activity, Constant.adNative_wifi_p,
                    adBean,
                    frameLayout, new AdManage.OnShowAdCompleteListener() {
                        @Override
                        public void onShowAdComplete() {

                        }

                        @Override
                        public void isMax() {

                        }
                    });
        }

    }

    @SuppressLint({"MissingInflatedId", "LocalSuppress"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_pwd);
        TextView wifiName = findViewById(R.id.wifi_name);
        wifiName.setText(wifiSSID);
        ImageView lookIv = findViewById(R.id.look_iv);
        EditText pwdEt = findViewById(R.id.pwd_et);
        ImageView chooseIv = findViewById(R.id.choose_iv);
        Button cancelBt = findViewById(R.id.cancel_button);
        Button connectBt = findViewById(R.id.connect_bt);
        frameLayout = findViewById(R.id.ad_fl);
        pwdEt.setText(pwd);
        loadNativeAd();
        lookIv.setOnClickListener(v -> {
            if (chooseIv.isSelected()) {
                chooseIv.setSelected(false);
                pwdEt.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                lookIv.setSelected(true);
            } else {
                pwdEt.setTransformationMethod(PasswordTransformationMethod.getInstance());
                lookIv.setSelected(false);
                chooseIv.setSelected(true);
            }
        });
        chooseIv.setOnClickListener(v -> {
            if (chooseIv.isSelected()) {
                chooseIv.setSelected(false);
                pwdEt.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                lookIv.setSelected(true);
            } else {
                pwdEt.setTransformationMethod(PasswordTransformationMethod.getInstance());
                lookIv.setSelected(false);
                chooseIv.setSelected(true);
            }
        });
        cancelBt.setOnClickListener(v -> dismiss());
        connectBt.setOnClickListener(v -> {
            if (pwdEt.getText().toString().length() > 0) {
                dismiss();
                connectWifi.connect(pwdEt.getText().toString());

            }
        });
        Window mWindow = getWindow();
        WindowManager.LayoutParams mParams = mWindow.getAttributes();
        mParams.alpha = 1f;
        mParams.gravity = Gravity.BOTTOM;
        mParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        mParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mParams.y = -200;
        mParams.x = 0;
        mWindow.setAttributes(mParams);
        mWindow.getDecorView().setBackgroundColor(Color.parseColor("#39000000"));
        mWindow.getDecorView().setPadding(0, 0, 0, 0);
        mWindow.getDecorView().setMinimumWidth(getContext().getResources().getDisplayMetrics().widthPixels);

    }

    public interface ConnectWifiListener {
        void connect(String pwd);
    }

}
