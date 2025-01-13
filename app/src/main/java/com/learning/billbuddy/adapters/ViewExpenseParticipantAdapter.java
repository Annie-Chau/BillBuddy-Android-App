package com.learning.billbuddy.adapters;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.learning.billbuddy.R;
import com.learning.billbuddy.models.Expense;
import com.learning.billbuddy.models.User;

import java.util.List;
import java.util.Map;

public class ViewExpenseParticipantAdapter extends RecyclerView.Adapter<ViewExpenseParticipantAdapter.ViewHolder> {

    private final Expense expense;
    private final List<User> userList;

    public ViewExpenseParticipantAdapter(Expense expense, List<User> userList) {
        this.expense = expense;
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_in_view_expense, parent, false); // Use your participant_item layout
        return new ViewHolder(view);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Double> participant = expense.getSplits().get(position);

        holder.participantName.setText(expense.getParticipantNameList(userList).get(position));

        // Get the participant's amount (which is the value in the map)
        double participantAmount = participant.values().iterator().next();
        holder.participantAmount.setText(expense.getCurrency() + " " + String.format("%.2f", participantAmount)); // Format as currency

        // ... set other views ...
    }

    @Override
    public int getItemCount() {
        return expense.getSplits().size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView participantName;
        public TextView participantAmount;
        // ... other views ...

        public ViewHolder(View itemView) {
            super(itemView);
            participantName = itemView.findViewById(R.id.participant_name);
            participantAmount = itemView.findViewById(R.id.amount);
            // ... initialize other views ...
        }
    }
}
