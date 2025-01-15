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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.learning.billbuddy.Api.CreateOrder;
import com.learning.billbuddy.Constant.AppInfo;
import com.learning.billbuddy.models.User;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class GoPremiumActivity extends AppCompatActivity {

    private static final String TAG = "CheckoutActivity";
    private static final String BACKEND_URL = "http://10.0.2.2:4242";

    private String paymentIntentClientSecret;
    private PaymentSheet paymentSheet;

    private Button selectPackageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_go_premium);
        Button backButton = findViewById(R.id.backButton);
        selectPackageButton = findViewById(R.id.selectPackageButton);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
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
                                    findViewById(R.id.premium_text).setVisibility(user.isPremium() ? View.VISIBLE : View.GONE);

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
        }

        selectPackageButton.setOnClickListener(this::onPayClicked);
        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);
        fetchPaymentIntent();

        backButton.setOnClickListener(v -> finish());
    }

    private void showAlert(String title, @Nullable String message) {
        runOnUiThread(() -> {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("Ok", null)
                    .create();
            dialog.show();
        });
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_LONG).show());
    }

    private void fetchPaymentIntent() {
        final String shoppingCartContent = "{\"items\": [ {\"id\":\"xl-tshirt\"}]}";

        final RequestBody requestBody = RequestBody.create(
                shoppingCartContent,
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(BACKEND_URL + "/create-payment-intent")
                .post(requestBody)
                .build();

        new OkHttpClient()
                .newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        showAlert("Failed to load data", "Error: " + e.toString());
                    }

                    @Override
                    public void onResponse(
                            @NonNull Call call,
                            @NonNull Response response
                    ) throws IOException {
                        if (!response.isSuccessful()) {
                            showAlert(
                                    "Failed to load page",
                                    "Error: " + response.toString()
                            );
                        } else {
                            final JSONObject responseJson = parseResponse(response.body());
                            paymentIntentClientSecret = responseJson.optString("clientSecret");
//                            runOnUiThread(() -> selectPackageButton.setEnabled(true));
                            Log.i(TAG, "Retrieved PaymentIntent");
                        }
                    }
                });
    }

    private JSONObject parseResponse(ResponseBody responseBody) {
        if (responseBody != null) {
            try {
                return new JSONObject(responseBody.string());
            } catch (IOException | JSONException e) {
                Log.e(TAG, "Error parsing response", e);
            }
        }

        return new JSONObject();
    }

    private void onPayClicked(View view) {
        PaymentSheet.Configuration configuration = new PaymentSheet.Configuration.Builder("Example, Inc.")
                .build();
        // Present Payment Sheet
        paymentSheet.presentWithPaymentIntent(paymentIntentClientSecret, configuration);
    }


    private void onPaymentSheetResult(
            final PaymentSheetResult paymentSheetResult
    ) {
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            User.upgradePremiumLifeTime(user.getUid());
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            showToast("Payment complete!");
        } else if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            Log.i(TAG, "Payment canceled!");
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            Throwable error = ((PaymentSheetResult.Failed) paymentSheetResult).getError();
            showAlert("Payment failed", error.getLocalizedMessage());
        }
    }

}