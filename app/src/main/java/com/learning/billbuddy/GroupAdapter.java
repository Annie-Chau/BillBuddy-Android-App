package com.learning.billbuddy;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.learning.billbuddy.R;
import com.learning.billbuddy.models.Group;

import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    private Context context;
    private List<Group> groupList;

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

        // Set click listener for the arrow button (if needed)
        holder.arrowButton.setOnClickListener(v -> {
            // Handle navigation or action when the arrow is clicked
            Intent intent = new Intent(context, ViewBalanceOfGroup.class);
            intent.putExtra("groupID", group.getGroupID());
            intent.putExtra("groupName", group.getName());
            intent.putExtra("groupDescription", group.getDescription());
            intent.putExtra("groupAvatarURL", group.getAvatarURL());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    public static class GroupViewHolder extends RecyclerView.ViewHolder {

        TextView groupName;
        ImageView groupImage;
        ImageButton arrowButton;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            groupName = itemView.findViewById(R.id.group_name);
            groupImage = itemView.findViewById(R.id.group_image);
            arrowButton = itemView.findViewById(R.id.group_card_action_button);

        }
    }
}
