package com.learning.billbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.learning.billbuddy.Api.CreateOrder;

import org.json.JSONObject;

import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class GoPremiumActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_go_premium);

        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // ZaloPay SDK Init
        ZaloPaySDK.init(554, Environment.SANDBOX);

        Button backButton = findViewById(R.id.backButton);
        Button selectPackageButton = findViewById(R.id.selectPackageButton);
        Button restorePremiumButton = findViewById(R.id.restorePremiumButton);

        backButton.setOnClickListener(v -> finish());

        selectPackageButton.setOnClickListener(v -> {

            CreateOrder orderApi = new CreateOrder();

            try {
                JSONObject data = orderApi.createOrder("200000");
                String code = data.getString("returncode");

                if (code.equals("1")) {
                    String token = data.getString("zptranstoken");
                    ZaloPaySDK.getInstance().payOrder(GoPremiumActivity.this, token, "demozpdk://app", new PayOrderListener() {

                        @Override
                        public void onPaymentSucceeded(String s, String s1, String s2) {
                            navigateToNotification("Payment Successful");
                        }

                        @Override
                        public void onPaymentCanceled(String s, String s1) {
                            navigateToNotification("Payment Canceled");
                        }

                        @Override
                        public void onPaymentError(ZaloPayError zaloPayError, String s, String s1) {
                            navigateToNotification("Payment Failed");
                        }
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        });

        restorePremiumButton.setOnClickListener(v ->
                Toast.makeText(GoPremiumActivity.this, "Premium restored!", Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        ZaloPaySDK.getInstance().onResult(intent);
    }

    private void navigateToNotification(String status) {
        Intent intent = new Intent(GoPremiumActivity.this, PaymentNotificationActivity.class);
        intent.putExtra("payment_status", status);
        startActivity(intent);
        finish();
    }
}