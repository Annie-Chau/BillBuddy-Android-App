package com.learning.billbuddy.utils;

import com.learning.billbuddy.models.Notification;

import java.util.List;

public interface NotificationCallback {
    void onCallback(List<Notification> notifications);
}