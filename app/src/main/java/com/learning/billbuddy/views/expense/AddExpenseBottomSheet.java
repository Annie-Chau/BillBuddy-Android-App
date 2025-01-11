package com.learning.billbuddy.views.expense;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.learning.billbuddy.R;
import com.learning.billbuddy.adapters.SplitAdapter;
import com.learning.billbuddy.models.Expense;
import com.learning.billbuddy.models.Group;
import com.learning.billbuddy.models.Notification;
import com.learning.billbuddy.models.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class AddExpenseBottomSheet extends BottomSheetDialogFragment {
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    EditText titleEditText, datePicker, amountEditText, description;
    Spinner paidBySpinner, currencySpinner;
    Button addButton, cancelButton;
    RecyclerView splitRecyclerView;

    private String selectedCurrency = "VND"; // Default currency
    private double amount;
    private String date;
    private Group currentGroup;
    private String paidBy;
    private String paidById;

    // Interface for communicating with parent
    public interface ExpenseAddedListener {
        void onExpenseAdded();
    }

    public AddExpenseBottomSheet() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_add_expense, container, false);

        titleEditText = view.findViewById(R.id.add_expense_enter_title);
        datePicker = view.findViewById(R.id.add_expense_date);
        amountEditText = view.findViewById(R.id.add_expense_money_amount);
        description = view.findViewById(R.id.add_expense_description);
        paidBySpinner = view.findViewById(R.id.add_expense_paid_by_spinner);
        currencySpinner = view.findViewById(R.id.add_expense_spinner_currency);
        addButton = view.findViewById(R.id.add_expense_btn_add);
        cancelButton = view.findViewById(R.id.add_expense_cancel_button);
        splitRecyclerView = view.findViewById(R.id.expense_list);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()); // Or your preferred format
        String today = dateFormat.format(calendar.getTime());
        datePicker.setText(today);

        cancelButton.setOnClickListener(v -> {
            dismiss();
        });

        currentGroup = (Group) getArguments().getSerializable("group");

        // Set up the RecyclerView
        splitRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        User.fetchAllUsers(users -> {
            SplitAdapter splitAdapter = new SplitAdapter(currentGroup.getMemberList(users), 0, splitRecyclerView, selectedCurrency);
            splitRecyclerView.setAdapter(splitAdapter);
        });

        datePicker.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    R.style.MyDateTimePickerDialogTheme,
                    (pickerView, year1, monthOfYear, dayOfMonth) -> {
                        calendar.set(year1, monthOfYear, dayOfMonth);

                        String selectedDate = dateFormat.format(calendar.getTime());

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
                Toast.makeText(requireContext(), "Invalid amount format", Toast.LENGTH_SHORT).show();
                return;
            }

            // Basic input validation (add more as needed)
            if (amount <= 0) {
                Toast.makeText(requireContext(), "Amount must be greater than 0", Toast.LENGTH_SHORT).show();
                return;
            }
            if (date.isEmpty()) {
                Toast.makeText(requireContext(), "Please select a date", Toast.LENGTH_SHORT).show();
                return;
            }
            if (paidBy == null || paidBy.isEmpty()) {
                Toast.makeText(requireContext(), "Please select who paid", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get split details from the adapter
            SplitAdapter splitAdapter = (SplitAdapter) splitRecyclerView.getAdapter();
            if (splitAdapter == null) {
                Toast.makeText(requireContext(), "Error getting split details", Toast.LENGTH_SHORT).show();
                return;
            }
            String expenseInfo = getExpenseInfo(splitAdapter);

            Log.d("Result", expenseInfo);

            Expense expense = new Expense(
                    UUID.randomUUID().toString(),
                    titleEditText.getText().toString(),
                    "",
                    amount,
                    description.getText().toString(),
                    "",
                    paidById,
                    SplitAdapter.getParticipantIDs(splitAdapter.getSplitItems()),
                    SplitAdapter.getSplits(splitAdapter.getSplitItems()),
                    new Date(),
                    selectedCurrency,
                    false
            );

            Expense.createExpense(
                    currentGroup.getGroupID(),
                    expense.getTitle(),
                    expense.getAvatarURL(),
                    expense.getAmount(),
                    expense.getNotes(),
                    expense.getBillPictureURL(),
                    expense.getPayerID(),
                    expense.getParticipantIDs(),
                    expense.getSplits(),
                    expense.getTimestamp(),
                    expense.getCurrency(),
                    false
            );

            // Notify all users in the group, including the payer
            notifyGroupMembers(expense);

            Toast.makeText(requireActivity(), "Expense added successfully", Toast.LENGTH_SHORT).show();
            dismiss();
        });

        amountEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                User.fetchAllUsers(users -> {
                    SplitAdapter splitAdapter;
                    String amountString = amountEditText.getText().toString();

                    if (!amountString.trim().isEmpty()) {
                        try {
                            double amount = Double.parseDouble(amountString);
                            splitAdapter = new SplitAdapter(currentGroup.getMemberList(users), amount, splitRecyclerView, selectedCurrency);
                        } catch (NumberFormatException e) {
                            splitAdapter = new SplitAdapter(currentGroup.getMemberList(users), 0.00, splitRecyclerView, selectedCurrency);
                        }
                    } else {
                        splitAdapter = new SplitAdapter(currentGroup.getMemberList(users), 0.00, splitRecyclerView, selectedCurrency);
                    }

                    splitRecyclerView.setAdapter(splitAdapter);
                    splitAdapter.notifyDataSetChanged();
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
            if (isAdded()){
                List<String> memberNames = currentGroup.getMemberNameList(users);

                ArrayAdapter<String> paidByAdapter = new ArrayAdapter<>(
                        requireContext(), android.R.layout.simple_spinner_item, memberNames);

                paidByAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                paidBySpinner.setAdapter(paidByAdapter);

                paidBySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                        paidBy = parentView.getItemAtPosition(position).toString();

                        // Get the selected User object
                        User selectedUser = currentGroup.getMemberList(users).get(position);

                        // Get the ID of the selected user
                        paidById = selectedUser.getUserID();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parentView) {
                        // Handle nothing selected
                    }
                });
            }
        });

        // Set up Currency spinner
        ArrayAdapter<CharSequence> currencyAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.currency, android.R.layout.simple_spinner_item); // Replace with your actual array resource
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currencySpinner.setAdapter(currencyAdapter);
        currencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedCurrency = parentView.getItemAtPosition(position).toString();

                User.fetchAllUsers(users -> {
                    if (isAdded()) {
                        SplitAdapter splitAdapter = new SplitAdapter(
                                currentGroup.getMemberList(users),
                                !amountEditText.getText().toString().isEmpty() ? Double.parseDouble(amountEditText.getText().toString()) : 0.00,
                                splitRecyclerView,
                                selectedCurrency);
                        splitRecyclerView.setAdapter(splitAdapter);
                        splitAdapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Handle nothing selected
            }
        });
        return view;
    }

    private @NonNull String getExpenseInfo(SplitAdapter splitAdapter) {
        List<SplitAdapter.SplitItem> splitItems = splitAdapter.getSplitItems();

        // Log the expense details (replace with your actual saving logic)
        StringBuilder expenseInfo = new StringBuilder("Expense saved:\n" +
                "Description: " + description.getText() + "\n" +
                "Amount: " + selectedCurrency + " " + amount + "\n" +
                "Date: " + date + "\n" +
                "Paid by: " + paidBy + "\n" +
                "Paid by ID: " + paidById + "\n");

        for (SplitAdapter.SplitItem item : splitItems) {
            expenseInfo.append(item.getUser().getName()).append(": ").append(selectedCurrency).append(" ").append(item.getAmount()).append("\n");
        }
        return expenseInfo.toString();
    }

    private void notifyGroupMembers(Expense expense) {
        List<String> allUserIDs = expense.getParticipantIDs();
        allUserIDs.add(expense.getPayerID());

        for (String userID : allUserIDs) {
            String notificationID = UUID.randomUUID().toString();
            String messageID = ""; // Assuming you have a message ID
            String type = "Expense";
            String message = "A new expense \"" + expense.getTitle() + "\" of " + expense.getCurrency() + " " + expense.getAmount() + " has been added.";
            Date timestamp = new Date();
            boolean isRead = false;

            Notification notification = new Notification(notificationID, messageID, type, message, timestamp, isRead);

            Log.d("AddExpenseBottomSheet", "Creating notification for user: " + userID);
            db.collection("notifications").document(notificationID).set(notification)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("AddExpenseBottomSheet", "Notification created successfully for user: " + userID);
                        // Add the notification ID to the user's document
                        db.collection("users").document(userID)
                                .update("notificationIds", FieldValue.arrayUnion(notificationID))
                                .addOnSuccessListener(aVoid2 -> Log.d("AddExpenseBottomSheet", "Notification ID added to user: " + userID))
                                .addOnFailureListener(e -> Log.e("AddExpenseBottomSheet", "Error adding notification ID to user", e));
                    })
                    .addOnFailureListener(e -> Log.e("AddExpenseBottomSheet", "Error creating notification", e));
        }
    }
}