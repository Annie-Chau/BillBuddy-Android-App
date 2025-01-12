package com.learning.billbuddy;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.learning.billbuddy.R;

public class PaymentNotificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_notification);

        TextView paymentNotificationResult = findViewById(R.id.payment_notification_result);
        Button paymentNotificationButton = findViewById(R.id.payment_notification_button);

        // Get the payment status from the intent
        String paymentStatus = getIntent().getStringExtra("payment_status");
        paymentNotificationResult.setText(paymentStatus);

        paymentNotificationButton.setOnClickListener(v -> finish());
    }
}