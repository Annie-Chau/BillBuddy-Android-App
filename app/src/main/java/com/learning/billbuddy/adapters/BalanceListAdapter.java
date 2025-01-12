package com.learning.billbuddy.adapters;


import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.learning.billbuddy.R;
import com.learning.billbuddy.models.Group;
import com.learning.billbuddy.models.User;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BalanceListAdapter extends RecyclerView.Adapter<BalanceListAdapter.AccountBalanceViewHolder> {
    private final Context context;

    private FirebaseUser currentUser;
    public List<Group.Reimbursement> reimbursementList;

    private Group group;

    private Map<String, User> userNameMap;

    public BalanceListAdapter(Context context, Group group, List<Group.Reimbursement> reimbursementList) {
        this.context = context;
        this.reimbursementList = reimbursementList;
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
        this.group = group;

    }

    // This allows the adapter to be notified when reimbursements change.
    public void updateReimbursements(List<Group.Reimbursement> newReimbursements) {
        this.reimbursementList = newReimbursements;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AccountBalanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_account_balance, parent, false);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();


        return new AccountBalanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountBalanceViewHolder holder, int position) {
        User.fetchAllUsers(users -> {
            //map id with its reference
            this.userNameMap = users.stream().collect(Collectors.toMap(User::getUserID, user -> user));


            String memberId = this.group.getMemberIDs().get(position);
            User user = this.userNameMap.get(memberId);

            if (user.getProfilePictureURL().equals("XXX") || user.getProfilePictureURL().isEmpty()) {
                Log.d("Profile Picture", "No profile picture found");
                holder.accountBalanceImage.setVisibility(View.GONE);
                holder.accountBalanceProfileTextView.setText(user.getName().substring(0, 1));
                holder.accountBalanceProfileTextView.setVisibility(View.VISIBLE);
            } else {
                holder.accountBalanceProfileTextView.setVisibility(View.GONE);
                holder.accountBalanceImage.setVisibility(View.VISIBLE);
                if (context instanceof Activity && !((Activity) context).isDestroyed()) {
                    Glide.with(context)
                            .load(user.getProfilePictureURL())
                            .circleCrop()
                            .into(holder.accountBalanceImage);
                }
            }

            holder.accountBalanceMemberName.setText(user.getName() + this.returnStringMeIfMatched(memberId));
            holder.accountBalanceAmount.setText("đ" + String.format("%.3f", Math.abs(getBalanceAmount(reimbursementList, memberId))));

            if (getBalanceAmount(reimbursementList, memberId) > 0) {
                holder.accountBalanceAmount.setTextColor(context.getResources().getColor(R.color.light_green));
            } else if (Math.round(getBalanceAmount(reimbursementList, memberId)) < 0) {
                holder.accountBalanceAmount.setTextColor(context.getResources().getColor(R.color.red));
            } else {
                holder.accountBalanceAmount.setTextColor(context.getResources().getColor(R.color.text_gray));
            }
        });


    }

    private String returnStringMeIfMatched(String memberId) {
        if (memberId.equals(this.currentUser.getUid())) {
            return " (me)";
        }
        return "";
    }

    private Double getBalanceAmount(List<Group.Reimbursement> reimbursements, String memberId) {
        Double amount = 0.0;
        for (Group.Reimbursement reimbursement : reimbursements) {
            if (reimbursement.getPayeeId().equals(memberId)) {
                amount -= reimbursement.getAmount();
            } else if (reimbursement.getPayerId().equals(memberId)) {
                amount += reimbursement.getAmount();
            }
        }

        return amount;
        // Logic to calculate the amount owed to currentLogin user
    }

    @Override
    public int getItemCount() {
        return this.group.getMemberIDs().size();
    }


    public static class AccountBalanceViewHolder extends RecyclerView.ViewHolder {
        LinearLayout expenseItemLinearLayout;
        ImageView accountBalanceImage;
        TextView accountBalanceProfileTextView;
        TextView accountBalanceMemberName;

        TextView accountBalanceAmount;

        public AccountBalanceViewHolder(@NonNull View itemView) {
            super(itemView);
            expenseItemLinearLayout = itemView.findViewById(R.id.expense_item_linear_layout);
            accountBalanceImage = itemView.findViewById(R.id.account_balance_image);
            accountBalanceProfileTextView = itemView.findViewById(R.id.account_balance_profile_text_view);
            accountBalanceMemberName = itemView.findViewById(R.id.account_balance_member_name);
            accountBalanceAmount = itemView.findViewById(R.id.account_balance_amount);
        }
    }
}
