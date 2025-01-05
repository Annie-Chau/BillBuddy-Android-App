package com.learning.billbuddy.utils;

import com.learning.billbuddy.models.Message;

import java.util.List;

public interface MessageCallback {
    void onCallback(List<Message> messages);
}