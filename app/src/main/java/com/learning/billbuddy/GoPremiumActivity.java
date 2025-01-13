package com.learning.billbuddy;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.learning.billbuddy.Api.CreateOrder;
import com.learning.billbuddy.Constant.AppInfo;
import com.learning.billbuddy.models.User;

import org.json.JSONObject;

import java.util.Objects;

import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class GoPremiumActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_go_premium);

        User.fetchAllUsers(users -> {
            users.stream()
                    .filter(user -> Objects.equals(user.getUserID(), Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()))
                    .findFirst()
                    .ifPresent(user -> findViewById(R.id.premium_text)
                            .setVisibility(user.isPremium() ? View.VISIBLE : View.GONE));
        });

        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // ZaloPay SDK Init
        ZaloPaySDK.init(AppInfo.APP_ID, Environment.SANDBOX);

        Button backButton = findViewById(R.id.backButton);
        Button selectPackageButton = findViewById(R.id.selectPackageButton);

        User.fetchAllUsers(users -> {
            users.stream()
                    .filter(user -> Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid().equals(user.getUserID()))
                    .findFirst()
                    .ifPresent(
                            user -> {
                                findViewById(R.id.free_plan).setSelected(!user.isPremium());
                                findViewById(R.id.premium_plan).setSelected(user.isPremium());

                                TextView freePlanTitle = findViewById(R.id.free_plan_title);
                                TextView premiumPlanTitle = findViewById(R.id.premium_plan_title);
                                freePlanTitle.setTextColor(!user.isPremium() ? Color.WHITE : Color.BLACK);
                                premiumPlanTitle.setTextColor(user.isPremium() ? Color.WHITE : Color.BLACK);

                                TextView freePlanText = findViewById(R.id.free_plan_text);
                                TextView premiumPlanText = findViewById(R.id.premium_plan_text);
                                freePlanText.setTextColor(!user.isPremium() ? Color.WHITE : Color.parseColor("#888888"));
                                premiumPlanText.setTextColor(user.isPremium() ? Color.WHITE : Color.parseColor("#888888"));

                                findViewById(R.id.premium_plan_offer).setVisibility(user.isPremium() ? View.GONE : View.VISIBLE);
                                findViewById(R.id.try_premium_text).setVisibility(user.isPremium() ? View.GONE : View.VISIBLE);

                                if (user.isPremium()) {
                                    selectPackageButton.setText("PREMIUM MEMBERSHIP ACTIVE");
                                    selectPackageButton.setTextColor(Color.BLACK);
                                    selectPackageButton.setEnabled(false);
                                    selectPackageButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#888888")));
                                } else {
                                    selectPackageButton.setText("UPGRADE TO PREMIUM MEMBERSHIP");
                                    selectPackageButton.setTextColor(Color.WHITE);
                                    selectPackageButton.setEnabled(true);
                                    selectPackageButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary)));
                                }
                            }
                    );
        });

        backButton.setOnClickListener(v -> finish());

        selectPackageButton.setOnClickListener(v -> {

            CreateOrder orderApi = new CreateOrder();

            try {
                JSONObject data = orderApi.createOrder("43000");
                String code = data.getString("returncode");

                if (code.equals("1")) {
                    String token = data.getString("zptranstoken");
                    ZaloPaySDK.getInstance().payOrder(GoPremiumActivity.this, token, "demozpdk://app", new PayOrderListener() {
                        @Override
                        public void onPaymentSucceeded(final String transactionId, final String transToken, final String appTransID) {
                            Log.d("AHAHAHAHA", "AHAHAHAHA");
                            Toast.makeText(GoPremiumActivity.this, "Thanh toán thành công", Toast.LENGTH_SHORT).show();
                            updateUserPremiumStatus();
                            navigateToNotification("Hello");
                        }

                        @Override
                        public void onPaymentCanceled(String zpTransToken, String appTransID) {
                            Toast.makeText(GoPremiumActivity.this, "Thanh toán bị hủy", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onPaymentError(ZaloPayError zaloPayError, String zpTransToken, String appTransID) {
                            Toast.makeText(GoPremiumActivity.this, "Thanh toán thất bại", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        });

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

    private void updateUserPremiumStatus() {
        FirebaseFirestore.getInstance().collection("users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .update("isPremium", true)
                .addOnSuccessListener(aVoid -> {
                    Log.d("PremiumStatus", "User's premium status updated successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e("PremiumStatus", "Error updating premium status", e);
                });
    }
}