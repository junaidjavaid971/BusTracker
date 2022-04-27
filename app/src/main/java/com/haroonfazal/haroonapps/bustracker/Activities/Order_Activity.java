package com.haroonfazal.haroonapps.bustracker.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.haroonfazal.haroonapps.bustracker.R;

public class Order_Activity extends AppCompatActivity {

    TextView tv_15Min, tv_30Min, tv_1Hour;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
setContentView(R.layout.order_layout);
        tv_15Min = findViewById(R.id.tv_15Min);
        tv_30Min = findViewById(R.id.tv_30Min);
        tv_1Hour = findViewById(R.id.tv_1Hour);

        tv_15Min.setOnClickListener(view -> {
            tv_15Min.setBackgroundResource(R.drawable.txt_black_circle);
            tv_30Min.setBackgroundResource(R.drawable.txt_gray_circle);
            tv_1Hour.setBackgroundResource(R.drawable.txt_gray_circle);
        });
        tv_30Min.setOnClickListener(view -> {
            tv_15Min.setBackgroundResource(R.drawable.txt_gray_circle);
            tv_30Min.setBackgroundResource(R.drawable.txt_black_circle);
            tv_1Hour.setBackgroundResource(R.drawable.txt_gray_circle);
        });

        tv_1Hour.setOnClickListener(view -> {
            tv_15Min.setBackgroundResource(R.drawable.txt_gray_circle);
            tv_30Min.setBackgroundResource(R.drawable.txt_gray_circle);
            tv_1Hour.setBackgroundResource(R.drawable.txt_black_circle);
        });

    }
}