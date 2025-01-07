package com.learning.billbuddy;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.learning.billbuddy.adapters.SplitAdapter;
import com.learning.billbuddy.models.Group;
import com.learning.billbuddy.models.User;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.List;

public class AddExpenseActivity extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    EditText datePicker, amountEditText, description;
    Spinner paidBySpinner, currencySpinner;
    Button addButton, cancelButton;
    RecyclerView splitRecyclerView;

    private String selectedCurrency = "đ"; // Default currency
    private String splitType;
    private double amount;
    private String date;
    private Group currentGroup;
    private String paidBy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        datePicker = findViewById(R.id.add_expense_date);
        amountEditText = findViewById(R.id.add_expense_money_amount);
        description = findViewById(R.id.add_expense_description);
        paidBySpinner = findViewById(R.id.add_expense_paid_by_spinner);
        currencySpinner = findViewById(R.id.add_expense_spinner_currency);
        addButton = findViewById(R.id.add_expense_btn_add);
        cancelButton = findViewById(R.id.add_expense_cancel_button);
        splitRecyclerView = findViewById(R.id.expense_list); // Initialize the RecyclerView

        cancelButton.setOnClickListener(v -> {
            finish();
        });

        currentGroup = (Group) getIntent().getSerializableExtra("group");

        // Set up the RecyclerView
        splitRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        User.fetchAllUsers(users -> {
            SplitAdapter splitAdapter = new SplitAdapter(currentGroup.getMemberList(users), 0, splitRecyclerView);
            splitRecyclerView.setAdapter(splitAdapter);
        });

        datePicker.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    R.style.MyDateTimePickerDialogTheme,
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        String selectedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1;
                        datePicker.setText(selectedDate);
                    },
                    year, month, day);

            datePickerDialog.show();
        });

        addButton.setOnClickListener(v -> {
            try {
                amount = Double.parseDouble(amountEditText.getText().toString());
                date = datePicker.getText().toString();
            } catch (NumberFormatException e) {
                // Handle invalid amount format
                Toast.makeText(AddExpenseActivity.this, "Invalid amount format", Toast.LENGTH_SHORT).show();
                return;
            }

            // Basic input validation (add more as needed)
            if (amount <= 0) {
                Toast.makeText(AddExpenseActivity.this, "Amount must be greater than 0", Toast.LENGTH_SHORT).show();
                return;
            }
            if (date.isEmpty()) {
                Toast.makeText(AddExpenseActivity.this, "Please select a date", Toast.LENGTH_SHORT).show();
                return;
            }
            if (splitType == null || splitType.isEmpty()) {
                Toast.makeText(AddExpenseActivity.this, "Please select split type", Toast.LENGTH_SHORT).show();
                return;
            }

            String expenseInfo = "Expense saved:\n" +
                    "Description: " + description.getText() + "\n" +
                    "Amount: " + selectedCurrency + " " + amount + "\n" +
                    "Date: " + date + "\n" +
                    "Split type: " + splitType;
            Log.d("Result", expenseInfo);
        });

        amountEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                User.fetchAllUsers(users -> {
                    SplitAdapter splitAdapter = new SplitAdapter(currentGroup.getMemberList(users), Double.parseDouble(amountEditText.getText().toString()), splitRecyclerView);
                    splitRecyclerView.setAdapter(splitAdapter);
                });
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    amount = Double.parseDouble(s.toString());
                } catch (NumberFormatException e) {
                    // Handle invalid format
                    amount = 0;
                }
            }
        });

        User.fetchAllUsers(users -> {
            List<String> memberNames = currentGroup.getMemberNameList(users);

            ArrayAdapter<String> paidByAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_item, memberNames);

            paidByAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            paidBySpinner.setAdapter(paidByAdapter);

            paidBySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    paidBy = parentView.getItemAtPosition(position).toString();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    // Handle nothing selected
                }
            });
        });

        // Set up Currency spinner
        ArrayAdapter<CharSequence> currencyAdapter = ArrayAdapter.createFromResource(this,
                R.array.currency, android.R.layout.simple_spinner_item); // Replace with your actual array resource
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currencySpinner.setAdapter(currencyAdapter);
        currencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedCurrency = parentView.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Handle nothing selected
            }
        });
    }

}