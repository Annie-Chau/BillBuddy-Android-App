package com.learning.billbuddy.views.faq;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.learning.billbuddy.R;

public class FAQPage extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the fragment layout
        View view = inflater.inflate(R.layout.fragment_faq, container, false);

        // FAQ Item 1
        LinearLayout faqItem1 = view.findViewById(R.id.faq_item_1);
        TextView faqAnswer1 = view.findViewById(R.id.faq_answer_1);
        ImageView faqIcon1 = view.findViewById(R.id.faq_icon_1);

        // FAQ Item 2
        LinearLayout faqItem2 = view.findViewById(R.id.faq_item_2);
        TextView faqAnswer2 = view.findViewById(R.id.faq_answer_2);
        ImageView faqIcon2 = view.findViewById(R.id.faq_icon_2);

        // Toggle FAQ Item 1
        faqItem1.setOnClickListener(v -> {
            if (faqAnswer1.getVisibility() == View.GONE) {
                faqAnswer1.setVisibility(View.VISIBLE); // Show the answer
                faqIcon1.setImageResource(R.drawable.arrow_up);
            } else {
                faqAnswer1.setVisibility(View.GONE); // Hide the answer
                faqIcon1.setImageResource(R.drawable.arrow_down);
            }
        });

        // Toggle FAQ Item 2
        faqItem2.setOnClickListener(v -> {
            if (faqAnswer2.getVisibility() == View.GONE) {
                faqAnswer2.setVisibility(View.VISIBLE); // Show the answer
                faqIcon2.setImageResource(R.drawable.arrow_up);
            } else {
                faqAnswer2.setVisibility(View.GONE); // Hide the answer
                faqIcon2.setImageResource(R.drawable.arrow_down);
            }
        });

        return view;
    }
}
