package com.learning.billbuddy.adapters;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.learning.billbuddy.R;
import com.learning.billbuddy.models.Expense;
import com.learning.billbuddy.models.Group;
import com.learning.billbuddy.views.expense.ViewReimbursementDetail;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


public class ReimbursementAdapter extends RecyclerView.Adapter<ReimbursementAdapter.ReimbursementViewHolder> {

    private final Context context;
    private final Handler handler;

    private FirebaseUser currentUser;
    public List<Group.Reimbursement> reimbursementList;

    public Map<String, String> userMap;

    private Group group;

    public ReimbursementAdapter(Context context, Handler handler, Group group, List<Group.Reimbursement> reimbursementList, Map<String, String> userMap) {
        this.context = context;
        this.handler = handler;
        this.reimbursementList = reimbursementList;
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
        this.userMap = userMap;
        this.group = group;

    }


    @NonNull
    @Override
    public ReimbursementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reimbursement, parent, false);
        return new ReimbursementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReimbursementViewHolder holder, int position) {
        Group.Reimbursement reimbursement = reimbursementList.get(position);

        // Set the reimbursement name
        String reimbursementName = this.userMap.get(reimbursement.getPayeeId()) +
                this.returnStringMeIfMatched(reimbursement.getPayeeId()) +
                " owes " +
                this.userMap.get(reimbursement.getPayerId()) +
                this.returnStringMeIfMatched(reimbursement.getPayerId());

        holder.reimbursementName.setText(reimbursementName);

        // Set the reimbursement amount
        holder.reimbursementAmount.setText("đ" + String.format("%.3f", reimbursement.getAmount()));

        // Set the other participants

        // Set the mark as paid button click listener
        handleMarkAsPaidButton(holder.markAsPaidButton, reimbursement);


        if (position == 0 && !isReimbursementPaidRelateCurrentUser(reimbursement)) {
            holder.textOther.setVisibility(View.VISIBLE);

        } else if (position > 0 && !isReimbursementPaidRelateCurrentUser(reimbursement) && isReimbursementPaidRelateCurrentUser(reimbursementList.get(position - 1))) {
            holder.textOther.setVisibility(View.VISIBLE);

        } else {
            holder.textOther.setVisibility(View.GONE);
        }
    }

    private void handleMarkAsPaidButton(Button button, Group.Reimbursement reimbursement) {
        button.setOnClickListener(v -> {

            // Get the current date
            ArrayList<String> participantIds = new ArrayList<>();
            participantIds.add(reimbursement.getPayerId());

            List<Map<String, Double>> splits = new ArrayList<>();
            splits.add(Map.of(reimbursement.getPayerId(), reimbursement.getAmount()));

            Expense.createExpense(
                    group.getGroupID(),
                    "Reimbursement",
                    "",
                    reimbursement.getAmount(),
                    "",
                    "",
                    reimbursement.getPayeeId(),
                    participantIds,
                    splits,
                    new Date(),
                    "VND",
                    true
            );
            notifyDataSetChanged();
            handler.sendEmptyMessage(1);
        });
    }

    private Boolean isReimbursementPaidRelateCurrentUser(Group.Reimbursement reimbursement) {
        return reimbursement.getPayeeId().equals(this.currentUser.getUid()) || reimbursement.getPayerId().equals(this.currentUser.getUid());
    }


    private String returnStringMeIfMatched(String memberId) {
        if (memberId.equals(this.currentUser.getUid())) {
            return " (me)";
        }
        return "";
    }

    @Override
    public int getItemCount() {
        return reimbursementList.size();
    }


    public static class ReimbursementViewHolder extends RecyclerView.ViewHolder {
        TextView textOther;
        TextView reimbursementName;
        TextView reimbursementAmount;
        Button markAsPaidButton;

        public ReimbursementViewHolder(@NonNull View itemView) {
            super(itemView);
            textOther = itemView.findViewById(R.id.reimbursement_text_others);
            reimbursementName = itemView.findViewById(R.id.reimbursement_item_name);
            reimbursementAmount = itemView.findViewById(R.id.reimbursement_item_amount);
            markAsPaidButton = itemView.findViewById(R.id.reimbursement_mark_as_paid);
        }
    }
}
