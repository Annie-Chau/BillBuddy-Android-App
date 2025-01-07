package com.learning.billbuddy;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;
import com.learning.billbuddy.adapters.ChatAdapter;
import com.learning.billbuddy.models.Chat;
import com.learning.billbuddy.models.Group;
import com.learning.billbuddy.models.Message;
import com.learning.billbuddy.models.User;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatBoxActivity extends AppCompatActivity {

    private static final String TAG = "ChatBoxActivity";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private List<Message> messageList;
    private EditText messageInput;
    private ImageButton sendButton;
    private ImageButton backButton;
    private TextView groupNameTextView;
    private String chatID;
    private String userID;
    private Map<String, String> userNames;
    private Group currentGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_box);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        backButton = findViewById(R.id.backButton);
        groupNameTextView = findViewById(R.id.groupName);

        currentGroup = (Group) getIntent().getSerializableExtra("group");

        userID = getIntent().getStringExtra("USER_ID");

        // Set the group name
        groupNameTextView.setText(currentGroup.getName());

        // Setup RecyclerView
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, messageList);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        userNames = new HashMap<>();
        fetchUserNames();

        checkOrCreateChat();

        sendButton.setOnClickListener(v -> sendMessage());

        backButton.setOnClickListener(v -> finish());
    }

    private void fetchUserNames() {
        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            User user = document.toObject(User.class);
                            userNames.put(user.getUserID(), user.getName());
                        }
                        chatAdapter.setUserNames(userNames);
                    }
                });
    }

    private void checkOrCreateChat() {
        db.collection("chats")
                .whereEqualTo("groupID", currentGroup.getGroupID())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Chat chat = document.toObject(Chat.class);
                            chatID = chat.getChatID();
                            Log.d(TAG, "Chat found with ID: " + chatID);
                            listenForMessages();
                            break;
                        }
                    } else {
                        createNewChat();
                    }
                });
    }

    private void createNewChat() {
        chatID = db.collection("chats").document().getId();
        Chat newChat = new Chat(chatID, currentGroup.getGroupID(), new ArrayList<>());
        db.collection("chats").document(chatID).set(newChat)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "New chat created with ID: " + chatID);
                    listenForMessages();
                });
    }

    private void listenForMessages() {
        db.collection("chats").document(chatID)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error listening for messages", e);
                        return;
                    }
                    if (snapshot != null && snapshot.exists()) {
                        Chat chat = snapshot.toObject(Chat.class);
                        if (chat != null) {
                            Log.d(TAG, "Message IDs: " + chat.getMessageIds());
                            loadMessages(chat.getMessageIds());
                        }
                    }
                });
    }

    private void loadMessages(List<String> messageIds) {
        if (messageIds != null && !messageIds.isEmpty()) {
            Log.d(TAG, "Loading messages with IDs: " + messageIds);
            db.collection("messages")
                    .whereIn("messageID", messageIds)
                    .orderBy("timestamp")
                    .addSnapshotListener((snapshots, e) -> {
                        if (e != null) {
                            Log.e(TAG, "Error loading messages", e);
                            return;
                        }
                        messageList.clear();
                        for (QueryDocumentSnapshot document : snapshots) {
                            Message message = document.toObject(Message.class);
                            Log.d(TAG, "Loaded message: " + message.getContent());
                            messageList.add(message);
                        }
                        chatAdapter.messageList = messageList;
                        chatAdapter.notifyDataSetChanged();
                        chatRecyclerView.scrollToPosition(messageList.size() - 1);
                    });
        } else {
            Log.d(TAG, "No messages to load");
        }
    }

    private void sendMessage() {
        String content = messageInput.getText().toString().trim();
        if (!TextUtils.isEmpty(content)) {
            String messageID = db.collection("messages").document().getId();
            Message message = new Message(messageID, userID, content, new Date());

            db.collection("messages").document(messageID).set(message)
                    .addOnSuccessListener(aVoid -> {
                        db.collection("chats").document(chatID)
                                .update("messageIds", FieldValue.arrayUnion(messageID))
                                .addOnSuccessListener(aVoid1 -> {
                                    messageInput.setText("");
                                    // No need to add the message to the list here, as it will be handled by the listener
                                });
                    });
        }
    }
}