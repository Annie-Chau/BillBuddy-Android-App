package com.learning.billbuddy.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.learning.billbuddy.R;
import com.learning.billbuddy.models.Expense; // Assuming you have an Expense model
import com.learning.billbuddy.models.User;

import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private Context context;
    private List<Expense> expenseList;

    public ExpenseAdapter(Context context, List<Expense> expenseList) {
        this.context = context;
        this.expenseList = expenseList;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_expense, parent, false); // Use your expense_item layout
        return new ExpenseViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenseList.get(position);

        // Set data to the views
        holder.expenseNameTextView.setText(expense.getTitle()); // Assuming your Expense model has a getName() method
        holder.expenseAmountTextView.setText(expense.getFormattedAmount()); // Assuming you have a getFormattedAmount() method in your Expense model
        User.fetchAllUsers(users -> {
            holder.paidByTextView.setText("Paid by " + expense.getPaidByName(users));
        });
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    public static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        ImageView expenseImageView;
        TextView expenseNameTextView;
        TextView expenseAmountTextView;
        TextView paidByTextView;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            expenseImageView = itemView.findViewById(R.id.expense_image);
            expenseNameTextView = itemView.findViewById(R.id.expense_name);
            expenseAmountTextView = itemView.findViewById(R.id.expense_amount);
            paidByTextView = itemView.findViewById(R.id.paid_by);
        }
    }
}
