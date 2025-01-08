package com.learning.billbuddy.adapters;

import android.content.Context;
import android.net.Uri;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.learning.billbuddy.R;
import com.learning.billbuddy.models.Message;
import com.learning.billbuddy.models.User;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private Context context;
    public List<Message> messageList;
    private Map<String, String> userNames;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public ChatAdapter(Context context, List<Message> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    public void setUserNames(Map<String, String> userNames) {
        this.userNames = userNames;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Message message = messageList.get(position);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = currentUser.getUid();

        if (message.getSenderID().equals(currentUserId)) {
            holder.leftChatLayout.setVisibility(View.GONE);
            holder.rightChatLayout.setVisibility(View.VISIBLE);
            holder.rightChatText.setText(message.getContent());
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            holder.rightChatTime.setText(sdf.format(message.getTimestamp()));
        } else {
            holder.rightChatLayout.setVisibility(View.GONE);
            holder.leftChatLayout.setVisibility(View.VISIBLE);
            holder.leftChatReceiverName.setText(userNames.get(message.getSenderID()));
            holder.leftChatText.setText(message.getContent());
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            holder.leftChatTime.setText(sdf.format(message.getTimestamp()));

            db.collection("users").document(message.getSenderID()).get().addOnCompleteListener(task -> {

                FirebaseUser userAuth = FirebaseAuth.getInstance().getCurrentUser();
                Log.d("ChatAdapter", "onBindViewHolder: " + userAuth.getPhotoUrl());

                if (task.isSuccessful()) {
                    User user = task.getResult().toObject(User.class);
                    if (user == null) {
                        return;
                    }

                    if (user.getProfilePictureURL() != null) {
                        try {
                            Glide.with(context).load(Uri.parse(user.getProfilePictureURL())).circleCrop().into(holder.leftChatAvatar);
                        } catch (Exception e) {
                            Log.d("ChatAdapter", "onBindViewHolder: " + e.getMessage());
                        }
                    }
                }
            });


        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {

        TextView leftChatReceiverName, leftChatText, leftChatTime;

        ImageView leftChatAvatar;
        TextView rightChatText, rightChatTime;

        LinearLayout leftChatLayout, rightChatLayout;


        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            leftChatLayout = itemView.findViewById(R.id.left_chat_layout);
            rightChatLayout = itemView.findViewById(R.id.right_chat_layout);


            leftChatReceiverName = itemView.findViewById(R.id.left_chat_receiver_name);
            leftChatText = itemView.findViewById(R.id.left_chat_text_view);
            leftChatTime = itemView.findViewById(R.id.left_chat_time);
            leftChatAvatar = itemView.findViewById(R.id.left_chat_image_view);

            rightChatText = itemView.findViewById(R.id.right_chat_text_view);
            rightChatTime = itemView.findViewById(R.id.right_chat_time);
        }
    }
}