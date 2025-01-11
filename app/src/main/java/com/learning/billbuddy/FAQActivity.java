package com.learning.billbuddy;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class FAQActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq); // Link to the updated XML layout

        // Initialize UI components
        ImageButton returnButton = findViewById(R.id.button_return);

        // FAQ Item 1
        LinearLayout faqItem1 = findViewById(R.id.faq_item_1);
        TextView faqAnswer1 = findViewById(R.id.faq_answer_1);
        View faqIcon1 = findViewById(R.id.faq_icon_1);

        // FAQ Item 2
        LinearLayout faqItem2 = findViewById(R.id.faq_item_2);
        TextView faqAnswer2 = findViewById(R.id.faq_answer_2);
        View faqIcon2 = findViewById(R.id.faq_icon_2);

        // Set the return button behavior
        returnButton.setOnClickListener(v -> finish()); // Close the activity and go back

        // Toggle FAQ Item 1
        faqItem1.setOnClickListener(v -> {
            if (faqAnswer1.getVisibility() == View.GONE) {
                faqAnswer1.setVisibility(View.VISIBLE); // Show the answer
                faqIcon1.setRotation(180); // Rotate the icon to indicate collapse
            } else {
                faqAnswer1.setVisibility(View.GONE); // Hide the answer
                faqIcon1.setRotation(0); // Reset the icon to indicate expand
            }
        });

        // Toggle FAQ Item 2
        faqItem2.setOnClickListener(v -> {
            if (faqAnswer2.getVisibility() == View.GONE) {
                faqAnswer2.setVisibility(View.VISIBLE); // Show the answer
                faqIcon2.setRotation(180); // Rotate the icon to indicate collapse
            } else {
                faqAnswer2.setVisibility(View.GONE); // Hide the answer
                faqIcon2.setRotation(0); // Reset the icon to indicate expand
            }
        });
    }
}