package com.learning.billbuddy.adapters;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.learning.billbuddy.R;
import com.learning.billbuddy.views.expense.ViewGroupDetailActivity;
import com.learning.billbuddy.interfaces.IResponseCallback;
import com.learning.billbuddy.models.Group;
import com.learning.billbuddy.models.Notification;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> implements RecyclerView.OnItemTouchListener {

    private final Context context;
    public List<Group> groupList;
    private FirebaseFirestore db;

    public GroupAdapter(Context context, List<Group> groupList) {
        this.context = context;
        this.groupList = groupList;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.group_card, parent, false);
        return new GroupViewHolder(view);
    }

    @SuppressLint({"ClickableViewAccessibility", "UseCompatLoadingForDrawables"})
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

        if (position == 0 || !groupList.get(position - 1).getCreatedDateStringFormat().equals(groupList.get(position).getCreatedDateStringFormat())) {
            Log.d("GroupAdapter", "onBindViewHolder: " + groupList.get(position).getCreatedDateStringFormat());
            Group currentGroup = groupList.get(position);
            if (currentGroup.getCreatedDateStringFormat().isEmpty()) {
                return;
            }
            holder.createdDate.setText(groupList.get(position).getCreatedDateStringFormat());
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
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.modal_dialog);

        Button cancelButton = dialog.findViewById(R.id.modal_dialog_cancel_button);
        Button confirmButton = dialog.findViewById(R.id.modal_dialog_confirm_button);

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        confirmButton.setOnClickListener(v -> {
            String currentUserID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
            if (currentUserID.equals(group.getOwnerID())) { // Assuming `getOwnerID()` returns ownerID
                try {
                    Group.deleteGroup(group, new IResponseCallback() {
                        @Override
                        public void onSuccess() {
                            notifyGroupMembers(group);
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
            } else {
                Toast.makeText(context, "You are not authorized to delete this group.", Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    private void notifyGroupMembers(Group group) {
        String ownerID = group.getOwnerID();
        String groupName = group.getName();
        List<String> memberIDs = group.getMemberIDs();

        for (String memberID : memberIDs) {
            if (!memberID.equals(ownerID)) {
                createNotificationForMember(memberID, groupName);
            }
        }
    }

    private void createNotificationForMember(String memberID, String groupName) {
        String notificationID = UUID.randomUUID().toString();
        String messageID = ""; // Assuming you have a message ID
        String type = "Group";
        String message = "The group \"" + groupName + "\" has been deleted.";
        Date timestamp = new Date();
        boolean isRead = false;

        Notification notification = new Notification(notificationID, messageID, type, message, timestamp, isRead);

        Log.d("GroupAdapter", "Creating notification for member: " + memberID);
        db.collection("notifications").document(notificationID).set(notification)
                .addOnSuccessListener(aVoid -> {
                    Log.d("GroupAdapter", "Notification created successfully for member: " + memberID);
                    // Add the notification ID to the user's document
                    db.collection("users").document(memberID)
                            .update("notificationIds", FieldValue.arrayUnion(notificationID))
                            .addOnSuccessListener(aVoid2 -> Log.d("GroupAdapter", "Notification ID added to user: " + memberID))
                            .addOnFailureListener(e -> Log.e("GroupAdapter", "Error adding notification ID to user", e));
                })
                .addOnFailureListener(e -> Log.e("GroupAdapter", "Error creating notification", e));
    }

    private void navigateItemGroupDetail(Group group) {
        Intent intent = new Intent(context, ViewGroupDetailActivity.class);
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
        CardView linearLayout;

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