package com.learning.billbuddy.models;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.learning.billbuddy.utils.UserCallback;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class User implements Serializable {

    private String userID; // Firebase UID
    private String name;
    private String email;
    private String phoneNumber;
    private String profilePictureURL; // URL from Firebase storage
    private String registrationMethod; // Enum{"Facebook", "Google", "Email", "Phone number"}
    private List<String> notificationIds;
    private boolean isPremium;

    // No-argument constructor
    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    // Constructor
    public User(String userID, String name, String email, String phoneNumber, String profilePictureURL, String registrationMethod, List<String> notificationIds, Boolean isPremium) {
        this.userID = userID;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.profilePictureURL = profilePictureURL;
        this.registrationMethod = registrationMethod;
        this.notificationIds = notificationIds;
        this.isPremium = isPremium;
    }

    // Getters and setters
    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProfilePictureURL() {
        return profilePictureURL;
    }

    public void setProfilePictureURL(String profilePictureURL) {
        this.profilePictureURL = profilePictureURL;
    }

    public String getRegistrationMethod() {
        return registrationMethod;
    }

    public void setRegistrationMethod(String registrationMethod) {
        this.registrationMethod = registrationMethod;
    }

    public List<String> getNotificationIds() {
        return notificationIds;
    }

    public void setNotificationIds(List<String> notificationIds) {
        this.notificationIds = notificationIds;
    }

    public boolean isPremium() {
        return isPremium;
    }

    public void setPremium(boolean premium) {
        isPremium = premium;
    }

    public static void fetchAllUsers(final UserCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Listening to real-time updates
        db.collection("users")
                .addSnapshotListener((value, error) -> {
                    List<User> result = new ArrayList<>();
                    if (error != null) {
                        Log.e("User Fetching", "Error listening to user updates: " + error);
                        callback.onCallback(result);
                        return;
                    }

                    if (value != null) {
                        for (DocumentSnapshot document : value.getDocuments()) {
                            result.add(new User(
                                    document.getString("userID"),
                                    document.getString("name"),
                                    document.getString("email"),
                                    document.getString("phoneNumber"),
                                    document.getString("profilePictureURL"),
                                    document.getString("registrationMethod"),
                                    (List<String>) document.get("notificationIds"),
                                    document.getBoolean("isPremium")
                            ));
                        }
                    }

                    callback.onCallback(result);
                });
    }

    // Method to create a new user
    public static void createUser(String userID, String name, String email, String phoneNumber, String profilePictureURL, String registrationMethod, List<String> notificationIds, boolean isPremium) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a new User object
        Map<String, Object> userData = new HashMap<>();
        userData.put("userID", userID);
        userData.put("name", name);
        userData.put("email", email);
        userData.put("phoneNumber", phoneNumber);
        userData.put("profilePictureURL", profilePictureURL);
        userData.put("registrationMethod", registrationMethod);
        userData.put("notificationIds", notificationIds);
        userData.put("isPremium", isPremium);

        // Add the new user to the "users" collection in Firestore
        db.collection("users")
                .document(userID)
                .set(userData)
                .addOnSuccessListener(documentReference -> Log.d("User Creation", "User created with ID: " + userID))
                .addOnFailureListener(e -> Log.e("User Creation", "Error creating user", e));
    }

    public static void updateUser(String userId, String userName, String phoneNumber, String profilePictureURL) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Log.d("User Update", "Updating user with ID: " + userId);
        Log.d("User Update", "Updating user userName: " + userName);
        Log.d("User Update", "Updating user phoneNumber: " + phoneNumber);
        db.collection("users")
                .document(userId)
                .update("name", userName, "phoneNumber", phoneNumber, "profilePictureURL", profilePictureURL)
                .addOnSuccessListener(aVoid -> Log.d("User Update", "User updated with ID: " + userId))
                .addOnFailureListener(e -> Log.e("User Update", "Error updating user", e));


    }

    public List<Notification> getUserNotifications(List<Notification> allNotifications) {
        return allNotifications.stream()
                .filter(notification -> this.notificationIds.contains(notification.getNotificationID()))
                .collect(Collectors.toList());
    }

    public static void getUsersByIds(List<String> userIds, IUsersCallBack callback) {
        fetchAllUsers(users -> {
            List<User> result = users.stream()
                    .filter(user -> userIds.contains(user.getUserID()))
                    .collect(Collectors.toList());
            callback.onSuccess(result);
        });
    }

    public void getNumberOfGroupPossessed(final GroupCountCallback callback) {
        Group.fetchAllGroups(groups -> {
            int count = 0;
            for (Group group : groups) {
                if (Objects.equals(group.getOwnerID(), this.userID)) {
                    count++;
                }
            }
            callback.onCallback(count);
        });

/*
        USAGE:
        user.getNumberOfGroupPossessed(count -> {
            Log.d("User Groups", "Number of groups owned by " + user.getName() + ": " + count);
            // Do something with the count, e.g., update UI
        });
*/
    }

    public interface GroupCountCallback {
        void onCallback(int count);
    }

    public interface IUsersCallBack {
        void onSuccess(List<User> users);
    }

    @NonNull
    @Override
    public String toString() {
        return "User{" +
                "userID='" + userID + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", profilePictureURL='" + profilePictureURL + '\'' +
                ", registrationMethod='" + registrationMethod + '\'' +
                ", notificationIds=" + notificationIds +
                ", isPremium=" + isPremium +
                '}';
    }
}
