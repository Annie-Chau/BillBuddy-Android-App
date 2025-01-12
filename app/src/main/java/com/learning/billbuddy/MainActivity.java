package com.learning.billbuddy;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.learning.billbuddy.models.Notification;
import com.learning.billbuddy.models.User;
import com.learning.billbuddy.views.authentication.Login;
import com.learning.billbuddy.views.faq.FAQPage;
import com.learning.billbuddy.views.aboutus.AboutUsPage;
import com.learning.billbuddy.views.home.HomePage;
import com.learning.billbuddy.views.profile.Profile;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FirebaseUser currentUser;
    private User currentUserData;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;

    private final NavigationBarView.OnItemSelectedListener navListener =
            item -> {
                int itemId = item.getItemId();
                Fragment selectedFragment = null;

                if (itemId == R.id.bottom_navigation_home) {
                    selectedFragment = new HomePage();
                } else if (itemId == R.id.bottom_navigation_profile) {
                    selectedFragment = new Profile();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("user", currentUserData);
                    selectedFragment.setArguments(bundle);
                } else if (itemId == R.id.bottom_navigation_about_us) { // go to about us page
                    selectedFragment = new AboutUsPage();
                } else if (itemId == R.id.bottom_navigation_faq) { // go to FAQ page
                    selectedFragment = new FAQPage();
                } else {
                    selectedFragment = new HomePage();
                }

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();

                return true;
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setNavigationBarColor(getColor(R.color.bottom_navigation));
        super.onCreate(savedInstanceState);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Check if user is logged in
        currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            // Redirect to LoginActivity if not logged in
            Intent intent = new Intent(MainActivity.this, Login.class);
            startActivity(intent);
            finish();
        } else {
            // User is logged in, proceed with fetching user data or other initialization
            setContentView(R.layout.activity_main);
            User.fetchAllUsers(users -> {
                for (User user : users) {
                    if (user.getUserID().equals(currentUser.getUid())) {
                        currentUserData = user;
                    }
                }
                // Start listening for notifications after fetching user data
                listenForNotifications();
            });
            BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
            bottomNav.setSelectedItemId(R.id.bottom_navigation_home);
            bottomNav.setOnItemSelectedListener(navListener);
            Fragment selectedFragment = new HomePage();

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    selectedFragment).commit();
        }

        // Request notification permissions
        requestNotificationPermission();
    }

    private void listenForNotifications() {
        db.collection("users").document(currentUser.getUid())
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error listening for notifications", e);
                        return;
                    }
                    if (snapshot != null && snapshot.exists()) {
                        User user = snapshot.toObject(User.class);
                        if (user != null) {
                            List<String> notificationIds = user.getNotificationIds();
                            for (String notificationID : notificationIds) {
                                db.collection("notifications").document(notificationID)
                                        .addSnapshotListener((documentSnapshot, error) -> {
                                            if (error != null) {
                                                Log.e(TAG, "Error listening for notification", error);
                                                return;
                                            }
                                            if (documentSnapshot != null && documentSnapshot.exists()) {
                                                Notification notification = documentSnapshot.toObject(Notification.class);
                                                if (notification != null && !notification.isRead()) {
                                                    // Display the notification to the user
                                                    displayNotification(notification);

                                                    // Mark the notification as read and delete it
                                                    db.collection("notifications").document(notificationID)
                                                            .update("isRead", true)
                                                            .addOnSuccessListener(aVoid -> {
                                                                db.collection("users").document(currentUser.getUid())
                                                                        .update("notificationIds", FieldValue.arrayRemove(notificationID));
                                                                db.collection("notifications").document(notificationID).delete();
                                                            });
                                                }
                                            }
                                        });
                            }
                        }
                    }
                });
    }

    private void displayNotification(Notification notification) {
        String messageID = notification.getMessageID();
        if (messageID == null || messageID.isEmpty()) {
            // Handle notifications without a messageID directly
            Log.d(TAG, "Displaying notification without message ID");

            // Create the notification
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            String channelId = "default_channel_id";
            String channelName = "Default Channel";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
                notificationManager.createNotificationChannel(channel);
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("Group Notification")
                    .setContentText(notification.getMessage())
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setAutoCancel(true);

            notificationManager.notify(notification.getNotificationID().hashCode(), builder.build());
            return;
        }

        Log.d(TAG, "Fetching message with ID: " + messageID);

        // Fetch the message details using the messageID from the notification
        db.collection("messages").document(messageID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String senderID = documentSnapshot.getString("senderID");
                        String messageContent = documentSnapshot.getString("content");

                        if (senderID == null) {
                            Log.e(TAG, "Sender ID is null for message: " + messageID);
                            return;
                        }

                        db.collection("users").document(senderID)
                                .get()
                                .addOnSuccessListener(userSnapshot -> {
                                    if (userSnapshot.exists()) {
                                        String senderName = userSnapshot.getString("name");

                                        // Log the notification details
                                        Log.d(TAG, "Displaying notification from " + senderName + ": " + messageContent);

                                        // Create the notification
                                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                        String channelId = "default_channel_id";
                                        String channelName = "Default Channel";

                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
                                            notificationManager.createNotificationChannel(channel);
                                        }

                                        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                                                .setSmallIcon(R.drawable.ic_notification)
                                                .setContentTitle("New message from " + senderName)
                                                .setContentText(messageContent)
                                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                                .setDefaults(NotificationCompat.DEFAULT_ALL)
                                                .setAutoCancel(true);

                                        notificationManager.notify(notification.getNotificationID().hashCode(), builder.build());
                                    } else {
                                        Log.e(TAG, "User snapshot does not exist for senderID: " + senderID);
                                    }
                                })
                                .addOnFailureListener(e -> Log.e(TAG, "Error fetching user data", e));
                    } else {
                        Log.e(TAG, "Message snapshot does not exist for messageID: " + messageID);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching message data", e));
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
        }
    }

}