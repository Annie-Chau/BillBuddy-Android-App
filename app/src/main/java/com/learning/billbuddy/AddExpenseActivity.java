package com.learning.billbuddy;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;

public class AddExpenseActivity extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    EditText datePicker, amountEditText, description;
    Spinner paidBySpinner, splitTypeSpinner, currencySpinner;
    Button saveButton, cancelButton;
    RecyclerView peopleRecyclerView;

    private String selectedCurrency = "VND"; // Default currency
    private String splitType;
    private double amount;
    private String date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        datePicker = findViewById(R.id.add_expense_date);
        amountEditText = findViewById(R.id.add_expense_money_amount);
        description = findViewById(R.id.add_expense_description);
        paidBySpinner = findViewById(R.id.add_expense_paid_by_spinner);
        currencySpinner = findViewById(R.id.add_expense_spinner_currency);
        saveButton = findViewById(R.id.add_expense_btn_add);
        cancelButton = findViewById(R.id.add_expense_cancel_button);

        cancelButton.setOnClickListener(v -> {
            finish();
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

        saveButton.setOnClickListener(v -> {
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

        // Set up Paid By spinner
//        ArrayAdapter<CharSequence> paidByAdapter = ArrayAdapter.createFromResource(this,
//                R.array.paid_by_options, android.R.layout.simple_spinner_item); // Replace with your actual array resource
//        paidByAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        paidBySpinner.setAdapter(paidByAdapter);
//        paidBySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
//                paidBy = parentView.getItemAtPosition(position).toString();
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parentView) {
//                // Handle nothing selected
//            }
//        });

        // Set up Split Type spinner
        ArrayAdapter<CharSequence> splitTypeAdapter = ArrayAdapter.createFromResource(this,
                R.array.split_type_options, android.R.layout.simple_spinner_item); // Replace with your actual array resource
        splitTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        splitTypeSpinner.setAdapter(splitTypeAdapter);
        splitTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                splitType = parentView.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Handle nothing selected
            }
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