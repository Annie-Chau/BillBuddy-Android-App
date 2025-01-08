package com.learning.billbuddy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.learning.billbuddy.adapters.ViewExpenseParticipantAdapter;
import com.learning.billbuddy.models.Expense;
import com.learning.billbuddy.models.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ViewExpenseDetailActivity extends AppCompatActivity {

    private TextView expenseName, expenseDate, expenseAmount, paidByName, paidByAmount, expenseDescription;
    private RecyclerView participantList;
    private Expense currentExpense;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_expense_detail); // Make sure this matches your XML filename

        // Initialize views
        ImageView expenseImage = findViewById(R.id.expense_image);
        expenseName = findViewById(R.id.expense_name);
        expenseDate = findViewById(R.id.expense_date);
        expenseAmount = findViewById(R.id.expense_amount);
        paidByName = findViewById(R.id.paid_by_name);
        paidByAmount = findViewById(R.id.paid_by_amount);
        participantList = findViewById(R.id.participant_list);
        expenseDescription = findViewById(R.id.expense_description);
        ImageButton returnButton = findViewById(R.id.return_button);
        ImageButton renameButton = findViewById(R.id.rename_button);

        currentExpense = (Expense) getIntent().getSerializableExtra("expense");

        // Set up button click listeners
        returnButton.setOnClickListener(v -> finish()); // Close the activity
        renameButton.setOnClickListener(v -> {
            // Handle rename button click (e.g., open an edit dialog)
        });

        expenseDescription.setText(currentExpense.getNotes());

        populateExpenseDetails();
        setupParticipantList();
    }

    @SuppressLint("SetTextI18n")
    private void populateExpenseDetails() {
        // Example: Populate views with data from the Expense object
        expenseName.setText(currentExpense.getTitle());
        expenseDate.setText(new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault()).format(currentExpense.getTimestamp()));
        expenseAmount.setText(currentExpense.getCurrency() + " " + String.format("%.2f", currentExpense.getAmount()));
        User.fetchAllUsers(users -> {
            paidByName.setText(currentExpense.getPaidByName(users));
        });
        paidByAmount.setText(currentExpense.getCurrency() + " " + String.format("%.2f", currentExpense.getAmount()));
        // ... set other views ...
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setupParticipantList() {
        User.fetchAllUsers(users -> {
            ViewExpenseParticipantAdapter adapter = new ViewExpenseParticipantAdapter(currentExpense, users);
            Log.d("Test", currentExpense.toString() + users);
            participantList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            participantList.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        });
    }
}