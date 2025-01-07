package com.learning.billbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class ViewBalanceOfGroup extends AppCompatActivity {

    private RadioGroup segmentGroup;
    private RadioButton rbExpense;
    private RadioButton rbBalance;
    private TextView groupNameTextView;
    private ImageView groupImageView;
    private FloatingActionButton chatButton;

    private String groupID;
    private ArrayList<String> memberIDs;
    private String groupName;

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
        chatButton = findViewById(R.id.chat_button);

        // Retrieve data from Intent
        groupID = getIntent().getStringExtra("groupID");
        groupName = getIntent().getStringExtra("groupName");
        String groupDescription = getIntent().getStringExtra("groupDescription");
        String groupAvatarURL = getIntent().getStringExtra("groupAvatarURL");
        memberIDs = getIntent().getStringArrayListExtra("memberIDs");

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

        // Set up chat button click listener
        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToChatBox();
            }
        });
    }

    private void navigateToChatBox() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userID = currentUser.getUid();
            Intent intent = new Intent(ViewBalanceOfGroup.this, ChatBoxActivity.class);
            intent.putExtra("GROUP_ID", groupID);
            intent.putStringArrayListExtra("MEMBER_IDS", memberIDs);
            intent.putExtra("USER_ID", userID);
            intent.putExtra("GROUP_NAME", groupName); // Pass the group name
            startActivity(intent);
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }
}