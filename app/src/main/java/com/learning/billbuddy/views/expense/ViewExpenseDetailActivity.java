package com.learning.billbuddy.views.expense;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.learning.billbuddy.R;
import com.learning.billbuddy.adapters.ViewExpenseParticipantAdapter;
import com.learning.billbuddy.models.Expense;
import com.learning.billbuddy.models.Group;
import com.learning.billbuddy.models.User;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ViewExpenseDetailActivity extends AppCompatActivity {

    private TextView expenseName, expenseDate, expenseAmount, paidByName, paidByAmount, expenseDescription, topheading, bottomHeading;
    private RecyclerView participantList;
    private Expense currentExpense;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setNavigationBarColor(getResources().getColor(R.color.background));
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
        topheading = findViewById(R.id.expense_detail_top_heading);
        bottomHeading = findViewById(R.id.expense_detail_bottom_heading);

        User.fetchAllUsers(users -> {
            users.stream()
                    .filter(user -> Objects.equals(user.getUserID(), Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()))
                    .findFirst()
                    .ifPresent(user -> findViewById(R.id.premium_text)
                            .setVisibility(user.isPremium() ? View.VISIBLE : View.GONE));
        });

        currentExpense = (Expense) getIntent().getSerializableExtra("expense");

        // Set up button click listeners
        returnButton.setOnClickListener(v -> finish()); // Close the activity
        renameButton.setOnClickListener(v -> {
            // Handle rename button click (e.g., open an edit dialog)
        });

        if (currentExpense.getIsReimbursed()) {
            topheading.setText("Transferred Form");
            bottomHeading.setText("To 1 Participant");
        } else {
            topheading.setText("Paid By");

            String bottomHeadingText = "For " + currentExpense.getParticipantIDs().size() + " Participants";
            if (currentExpense.getParticipantIDs().contains(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                bottomHeadingText += ", Including Me";
            }
            bottomHeading.setText(bottomHeadingText);
        }

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
            participantList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            participantList.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        });
    }

}