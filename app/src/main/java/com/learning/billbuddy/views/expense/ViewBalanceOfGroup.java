package com.learning.billbuddy.views.expense;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.learning.billbuddy.AddExpenseActivity;
import com.learning.billbuddy.ChatBoxActivity;
import com.learning.billbuddy.R;
import com.learning.billbuddy.adapters.ExpenseAdapter;
import com.learning.billbuddy.models.Expense;
import com.learning.billbuddy.models.Group;
import com.learning.billbuddy.views.home.AddGroupBottomSheetDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ViewBalanceOfGroup extends AppCompatActivity {

    private RadioGroup segmentGroup;
    private RadioButton rbExpense, rbBalance;
    private TextView groupNameTextView;
    private ImageView groupImageView;
    private FloatingActionButton chatButton;
    private Group currentGroup;
    private RecyclerView expenseRecyclerView;
    private ExpenseAdapter expenseAdapter;
    private List<Expense> expenseList = new ArrayList<>();

    @SuppressLint("NotifyDataSetChanged")
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
        expenseRecyclerView = findViewById(R.id.expense_list);
        expenseRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        expenseAdapter = new ExpenseAdapter(this, expenseList);
        expenseRecyclerView.setAdapter(expenseAdapter);

        // Retrieve data from Intent
        currentGroup = (Group) getIntent().getSerializableExtra("group");

        rbExpense.setChecked(true);
        findViewById(R.id.expense_content).setVisibility(View.VISIBLE);
        findViewById(R.id.balance_content).setVisibility(View.GONE);

        // Set data to views
        groupNameTextView.setText(currentGroup.getName());

        if (currentGroup.getAvatarURL() != null && !currentGroup.getAvatarURL().isEmpty()) {
            Glide.with(this).load(currentGroup.getAvatarURL()).into(groupImageView);
        }

        Expense.fetchAllExpenses(expenses -> {
            // Filter the expenses list to include only those with IDs in groupExpenseIDs
            expenseList = expenses.stream()
                    .filter(expense -> currentGroup.getExpenseIDs().contains(expense.getExpenseID()))
                    .collect(Collectors.toList());

            expenseAdapter.setExpenseList(expenseList);
            expenseAdapter.notifyDataSetChanged();
        });

        segmentGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_expense) {
                findViewById(R.id.expense_content).setVisibility(View.VISIBLE);
                findViewById(R.id.balance_content).setVisibility(View.GONE);
            } else if (checkedId == R.id.rb_balance) {
                findViewById(R.id.expense_content).setVisibility(View.GONE);
                findViewById(R.id.balance_content).setVisibility(View.VISIBLE);
            }
        });

        // Set up chat button click listener
        chatButton.setOnClickListener(v -> navigateToChatBox());

        findViewById(R.id.return_button).setOnClickListener(v -> finish());

        findViewById(R.id.to_add_expense_btn).setOnClickListener(v -> {
            AddExpenseBottomSheet bottomSheet = new AddExpenseBottomSheet();
            Bundle args = new Bundle();
            args.putSerializable("group", currentGroup);
            bottomSheet.setArguments(args);
            bottomSheet.show(ViewBalanceOfGroup.this.getSupportFragmentManager(), "AddGroupBottomSheetDialog");
        });
    }

    private void navigateToChatBox() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userID = currentUser.getUid();
            Intent intent = new Intent(ViewBalanceOfGroup.this, ChatBoxActivity.class);
            intent.putExtra("group", currentGroup);
            intent.putExtra("USER_ID", userID);
            startActivity(intent);
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }
}