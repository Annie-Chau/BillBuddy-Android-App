package com.learning.billbuddy;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

public class ViewBalanceOfGroup extends AppCompatActivity {

    private RadioGroup segmentGroup;
    private RadioButton rbExpense;
    private RadioButton rbBalance;
    private TextView groupNameTextView;
    private ImageView groupImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_balance_of_group);

        // Initialize UI elements
        segmentGroup = findViewById(R.id.segment_button_group);
        rbExpense = findViewById(R.id.rb_expense);
        rbBalance = findViewById(R.id.rb_balance);
        groupNameTextView = findViewById(R.id.group_name);
        groupImageView = findViewById(R.id.group_image);

        // Retrieve data from Intent
        String groupID = getIntent().getStringExtra("groupID");
        String groupName = getIntent().getStringExtra("groupName");
        String groupDescription = getIntent().getStringExtra("groupDescription");
        String groupAvatarURL = getIntent().getStringExtra("groupAvatarURL");

        // Set data to views
        groupNameTextView.setText(groupName);

        if (groupAvatarURL != null && !groupAvatarURL.isEmpty()) {
            Glide.with(this).load(groupAvatarURL).into(groupImageView);
        }

        segmentGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_expense) {
                Toast.makeText(this, "Expense Selected", Toast.LENGTH_SHORT).show();
            } else if (checkedId == R.id.rb_balance) {
                Toast.makeText(this, "Balance Selected", Toast.LENGTH_SHORT).show();
            }
        });
    }
}