package com.learning.billbuddy;

import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ViewBalanceOfGroup extends AppCompatActivity {

    private RadioGroup segmentGroup;
    private RadioButton rbExpense;
    private RadioButton rbBalance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_balance_of_group);

        segmentGroup = findViewById(R.id.segment_button_group);
        rbExpense = findViewById(R.id.rb_expense);
        rbBalance = findViewById(R.id.rb_balance);

        segmentGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_expense) {
                Toast.makeText(this, "Expense Selected", Toast.LENGTH_SHORT).show();
            } else if (checkedId == R.id.rb_balance) {
                Toast.makeText(this, "Balance Selected", Toast.LENGTH_SHORT).show();
            }
        });
    }
}