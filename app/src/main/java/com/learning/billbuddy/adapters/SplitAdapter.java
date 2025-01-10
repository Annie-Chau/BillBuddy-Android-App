package com.learning.billbuddy.adapters;

import com.learning.billbuddy.R;
import com.learning.billbuddy.models.User;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SplitAdapter extends RecyclerView.Adapter<SplitAdapter.ViewHolder> {

    private static List<User> personList;
    private static double totalAmount = 0; // Store the total amount
    private static int checked;
    private static RecyclerView splitRecyclerView;
    private String currencySymbol = "VND"; // Default currency symbol

    // Add SplitItem class
    public static class SplitItem {
        private User user;
        private double amount;

        public SplitItem(User user, double amount) {
            this.user = user;
            this.amount = amount;
        }

        public User getUser() {
            return user;
        }

        public double getAmount() {
            return amount;
        }
    }

    public SplitAdapter(List<User> personList, double totalAmount, RecyclerView splitRecyclerView, String currencySymbol) {
        this.personList = personList;
        this.totalAmount = totalAmount;
        this.checked = personList.size();
        this.splitRecyclerView = splitRecyclerView;
        this.currencySymbol = currencySymbol;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_in_expense, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User person = personList.get(position);
        holder.personNameTextView.setText(person.getName());
        holder.personAmountTextView.setText(currencySymbol + " " + String.format("%.2f", calculateSplitAmount()));
        holder.checkBox.setChecked(true);

        holder.checkBox.setOnClickListener(v -> {
            if (holder.checkBox.isChecked()) {
                checked++;
            } else {
                checked--;
            }
            updateSplitAmounts();
        });
    }

    public int getItemCount() {
        return personList.size(); // Return the actual size of the list
    }

    private static double calculateSplitAmount() {
        if (checked == 0) return 0; // Prevent division by zero
        return totalAmount / checked;
    }

    // Method to update the total amount and recalculate the split amounts
    public void updateTotalAmount(double newTotalAmount) {
        totalAmount = newTotalAmount;
        updateSplitAmounts();
    }

    // Method to update the split amounts for all checked items
    private void updateSplitAmounts() {
        for (int i = 0; i < getItemCount(); i++) {
            ViewHolder itemHolder = (ViewHolder) splitRecyclerView.findViewHolderForAdapterPosition(i);
            if (itemHolder != null) {
                if (itemHolder.checkBox.isChecked()) {
                    itemHolder.personAmountTextView.setText(currencySymbol + " " + String.format("%.2f", calculateSplitAmount()));
                } else {
                    itemHolder.personAmountTextView.setText(currencySymbol + " " + String.format("%.2f", 0.0));
                }
            }
        }
    }

    // Implement getSplitItems()
    public List<SplitItem> getSplitItems() {
        List<SplitItem> splitItems = new ArrayList<>();
        for (int i = 0; i < getItemCount(); i++) {
            ViewHolder itemHolder = (ViewHolder) splitRecyclerView.findViewHolderForAdapterPosition(i);
            if (itemHolder != null && itemHolder.checkBox.isChecked()) {
                User user = personList.get(i);
                double amount = calculateSplitAmount();
                splitItems.add(new SplitItem(user, amount));
            }
        }
        return splitItems;
    }

    public static List<String> getParticipantIDs(List<SplitItem> splitItems) {
        return splitItems.stream()
                .map(item -> item.user.getUserID())
                .collect(Collectors.toList());
    }

    public static List<Map<String, Double>> getSplits(List<SplitItem> splitItems) {
        return splitItems.stream()
                .map(item -> {
                    Map<String, Double> split = new HashMap<>();
                    split.put(item.getUser().getUserID(), item.getAmount());
                    return split;
                })
                .collect(Collectors.toList());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView personNameTextView;
        TextView personAmountTextView;
        // ... (Add other views from your item layout)

        public ViewHolder(View view) {
            super(view);
            checkBox = view.findViewById(R.id.item_in_expense_checkbox);
            personNameTextView = view.findViewById(R.id.item_in_expense_person_name);
            personAmountTextView = view.findViewById(R.id.item_in_expense_person_amount);
            // ... (Initialize other views)
        }
    }
}