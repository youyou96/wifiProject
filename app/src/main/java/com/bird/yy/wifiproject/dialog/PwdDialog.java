package com.bird.yy.wifiproject.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bird.yy.wifiproject.R;
import com.bird.yy.wifiproject.utils.Constant;

public class PwdDialog extends Dialog {
    private ConnectWifiListener connectWifi;
    private String wifiSSID;
    private String pwd;

    public PwdDialog(@NonNull Context context,String wifiSSID,String pwd) {
        super(context);
        this.wifiSSID = wifiSSID;
        this.pwd = pwd;
    }

    public void setConnectWifi(ConnectWifiListener connectWifi) {
        this.connectWifi = connectWifi;
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
        pwdEt.setText(pwd);
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
                connectWifi.connect(pwdEt.getText().toString());
                dismiss();
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
        mWindow.getDecorView().setPadding(0,0,0,0);
        mWindow.getDecorView().setMinimumWidth(getContext().getResources().getDisplayMetrics().widthPixels);
    }

   public interface ConnectWifiListener {
        void connect(String pwd);
    }
}
