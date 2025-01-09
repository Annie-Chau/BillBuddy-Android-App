package com.learning.billbuddy.adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.learning.billbuddy.R;
import com.learning.billbuddy.views.expense.ViewBalanceOfGroup;
import com.learning.billbuddy.interfaces.IResponseCallback;
import com.learning.billbuddy.models.Group;

import java.util.List;


public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> implements RecyclerView.OnItemTouchListener {

    private final Context context;
    public List<Group> groupList;

    public GroupAdapter(Context context, List<Group> groupList) {
        this.context = context;
        this.groupList = groupList;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.group_card, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group group = groupList.get(position);

        // Set the group name
        holder.groupName.setText(group.getName());

        // Load the group image (use a library like Glide or Picasso for URLs)
        if (group.getAvatarURL() != null && !group.getAvatarURL().isEmpty()) {
            Glide.with(context)
                    .load(group.getAvatarURL())
                    .placeholder(R.drawable.example_image_1) // Default image
                    .into(holder.groupImage);
        } else {
            holder.groupImage.setImageResource(R.drawable.example_image_1); // Default image
        }

        if (position == 0 || !groupList.get(position - 1).getCreatedStringFormat().equals(groupList.get(position).getCreatedStringFormat())) {
            Log.d("GroupAdapter", "onBindViewHolder: " + groupList.get(position).getCreatedStringFormat());
            Group currentGroup = groupList.get(position);
            if (currentGroup.getCreatedStringFormat().isEmpty()) {
                return;
            }
            holder.createdDate.setText(groupList.get(position).getCreatedStringFormat());
            holder.createdDate.setVisibility(View.VISIBLE);
        } else {
            holder.createdDate.setVisibility(View.GONE);
        }

        // Set click listener for the arrow button (if needed)
        holder.arrowButton.setOnClickListener(v -> {
            // Handle navigation or action when the arrow is clicked
            navigateItemGroupDetail(group);
        });

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

        holder.linearLayout.setOnLongClickListener(v -> {
            showConfirmDeleteDialog(group);
            return true;
        });


        holder.linearLayout.setOnClickListener(v -> {
            // Handle click on the card
            Log.d("GroupAdapter", "Clicked on group: " + group.getName());
            navigateItemGroupDetail(group);
        });
    }

    private void showConfirmDeleteDialog(Group group) {
        // Show a dialog to confirm deletion
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.modal_dialog);

        Button cancelButton = dialog.findViewById(R.id.modal_dialog_cancel_button);
        Button confirmButton = dialog.findViewById(R.id.modal_dialog_confirm_button);

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        confirmButton.setOnClickListener(v -> {

            try {
                Group.deleteGroup(group, new IResponseCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(context, "Group deleted successfully", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(context, "Error deleting group: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.d("GroupAdapter", "Error deleting group: " + e.getMessage());
            }
            dialog.dismiss();
        });


        dialog.show();


    }

    private void navigateItemGroupDetail(Group group) {
        Intent intent = new Intent(context, ViewBalanceOfGroup.class);
        intent.putExtra("group", group);
        context.startActivity(intent);
    }


    @Override
    public int getItemCount() {
        return groupList.size();
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        return false;
    }

    @Override
    public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                rv.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                break;
            case MotionEvent.ACTION_UP:
                rv.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY_RELEASE);
                break;
        }

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    public static class GroupViewHolder extends RecyclerView.ViewHolder {

        TextView groupName;
        ImageView groupImage;
        ImageButton arrowButton;

        TextView createdDate;
        LinearLayout linearLayout;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            groupName = itemView.findViewById(R.id.group_name);
            groupImage = itemView.findViewById(R.id.group_image);
            arrowButton = itemView.findViewById(R.id.group_card_action_button);
            createdDate = itemView.findViewById(R.id.created_date_group);

            linearLayout = itemView.findViewById(R.id.group_card);
        }
    }
}
