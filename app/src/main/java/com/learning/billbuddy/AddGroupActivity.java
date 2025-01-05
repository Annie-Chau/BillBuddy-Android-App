package com.learning.billbuddy;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class AddGroupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);

        // Retrieve the data passed from HomePage
        String ownerID = getIntent().getStringExtra("OWNER_ID");
        String ownerName = getIntent().getStringExtra("OWNER_NAME");

        // Display the creator's name
        TextView creatorNameTextView = findViewById(R.id.creator_name);
        creatorNameTextView.setText(ownerName);
    }
}