package com.learning.billbuddy.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.learning.billbuddy.R;
import com.learning.billbuddy.views.expense.ViewExpenseDetailActivity;
import com.learning.billbuddy.models.Expense; // Assuming you have an Expense model
import com.learning.billbuddy.models.User;

import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private Context context;
    public List<Expense> expenseList;

    public ExpenseAdapter(Context context, List<Expense> expenseList) {
        this.context = context;
        this.expenseList = expenseList;
    }

    public void setExpenseList(List<Expense> expenseList) {
        this.expenseList = expenseList;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_expense, parent, false); // Use your expense_item layout
        return new ExpenseViewHolder(view);
    }

    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenseList.get(position);

        // Set data to the views
        holder.expenseNameTextView.setText(expense.getTitle()); // Assuming your Expense model has a getName() method
        holder.expenseAmountTextView.setText(expense.getFormattedAmount()); // Assuming you have a getFormattedAmount() method in your Expense model
        User.fetchAllUsers(users -> {
            holder.paidByTextView.setText(getPrefix(expense) + expense.getPaidByName(users));
        });

        if (position == 0) {
            holder.createdDate.setVisibility(View.VISIBLE);
            holder.createdDate.setText(expense.getTimeString());
        } else if (position > 0 && !expense.getTimeString().equals(expenseList.get(position - 1).getTimeString())) {
            holder.createdDate.setVisibility(View.VISIBLE);
            holder.createdDate.setText(expense.getTimeString());
        } else {
            holder.createdDate.setVisibility(View.GONE);
        }
        if (expense.getIsReimbursed()) {
            holder.expenseImageView.setImageDrawable(context.getDrawable(R.drawable.card));
        } else {
            holder.expenseImageView.setImageDrawable(context.getDrawable(R.drawable.money));
        }

        holder.linearLayout.setOnTouchListener((view, motionEvent) -> {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // Handle touch (press)
                    holder.linearLayout.setBackgroundColor(context.getColor(R.color.profile_page_gray));
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // Handle release
                    holder.linearLayout.setBackground(context.getDrawable(R.drawable.small_round_white)); // Reset color or set a new one
                    break;
            }
            return false;
        });

        holder.linearLayout.setOnClickListener(v -> {
            Intent intent = new Intent(context, ViewExpenseDetailActivity.class);

            // Pass the entire Expense object
            intent.putExtra("expense", expense);

            context.startActivity(intent);
        });
    }


    private String getPrefix(Expense expense) {
        return expense.getIsReimbursed() ? "Transferred By: " : "Paid By: ";
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

        TextView createdDate;

        LinearLayout linearLayout;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            expenseImageView = itemView.findViewById(R.id.expense_image);
            expenseNameTextView = itemView.findViewById(R.id.expense_name);
            expenseAmountTextView = itemView.findViewById(R.id.expense_amount);
            paidByTextView = itemView.findViewById(R.id.paid_by);
            createdDate = itemView.findViewById(R.id.expense_created_date);
            linearLayout = itemView.findViewById(R.id.expense_item_linear_layout);
        }
    }
}
