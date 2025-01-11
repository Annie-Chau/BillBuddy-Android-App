package com.learning.billbuddy.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.learning.billbuddy.R;

import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

    // Interface to handle remove button clicks
    public interface OnRemoveClickListener {
        void onRemoveClick(int position);
    }

    private List<String> memberNames;
    private OnRemoveClickListener listener;

    // Constructor
    public MemberAdapter(List<String> memberNames, OnRemoveClickListener listener) {
        this.memberNames = memberNames;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_member, parent, false);
        return new MemberViewHolder(v, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        String memberName = memberNames.get(position);
        if (position == 0) {
            holder.memberNameTextView.setText(memberName + " (Owner)");
            holder.removeButton.setVisibility(View.GONE); // Owner cannot be removed
        } else {
            holder.memberNameTextView.setText(memberName);
            holder.removeButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return memberNames.size();
    }

    // ViewHolder class
    public static class MemberViewHolder extends RecyclerView.ViewHolder {
        TextView memberNameTextView;
        ImageButton removeButton;

        public MemberViewHolder(@NonNull View itemView, OnRemoveClickListener listener) {
            super(itemView);
            memberNameTextView = itemView.findViewById(R.id.member_name);
            removeButton = itemView.findViewById(R.id.remove_member_button);

            // Set click listener for the remove button
            removeButton.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onRemoveClick(position);
                    }
                }
            });
        }
    }

    // Method to update the list
    public void updateList(List<String> newList) {
        memberNames = newList;
        notifyDataSetChanged();
    }
}