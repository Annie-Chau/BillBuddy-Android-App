package com.learning.billbuddy.adapters;

import com.learning.billbuddy.R;
import com.learning.billbuddy.models.User;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SplitAdapter extends RecyclerView.Adapter<SplitAdapter.ViewHolder> {

    private final List<User> personList;
    private double totalAmount = 0; // Store the total amount
    private int checked;
    private RecyclerView splitRecyclerView;

    public SplitAdapter(List<User> personList, double totalAmount, RecyclerView splitRecyclerView) {
        this.personList = personList;
        this.totalAmount = totalAmount;
        this.checked = 0;
        this.splitRecyclerView = splitRecyclerView;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_in_expense, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User person = personList.get(position);
        holder.personNameTextView.setText(person.getName());

        // Initialize amount to 0
        holder.personAmountTextView.setText("₫ 0.00");

        // Handle CheckBox clicks
        holder.checkBox.setOnClickListener(v -> {
            if (holder.checkBox.isChecked()) {
                checked++;
            } else {
                checked--;
            }

            // Update the amount for all checked items
            for (int i = 0; i < getItemCount(); i++) {
                ViewHolder itemHolder = (ViewHolder) splitRecyclerView.findViewHolderForAdapterPosition(i);
                if (itemHolder != null && itemHolder.checkBox.isChecked()) {
                    itemHolder.personAmountTextView.setText("₫ " + String.format("%.2f", calculateSplitAmount()));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return personList.size(); // Return the actual size of the list
    }

    private double calculateSplitAmount() {
        return totalAmount / checked;
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


