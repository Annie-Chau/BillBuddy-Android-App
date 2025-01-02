package com.learning.billbuddy;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.firestore.FirebaseFirestore;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class AddExpense extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        Button cancelButton = findViewById(R.id.add_expense_cancel_button);
        EditText selectDate = findViewById(R.id.add_expense_date_picker);
        Button saveButton = findViewById(R.id.add_expense_save_button);

        cancelButton.setOnClickListener(v -> {
            finish();
        });

        selectDate.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    R.style.MyDateTimePickerDialogTheme,
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        String selectedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1;
                        selectDate.setText(selectedDate);
                    },
                    year, month, day);

            datePickerDialog.show();
        });

        saveButton.setOnClickListener(v -> {

        });
    }
}