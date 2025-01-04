package com.learning.billbuddy.utils;

import com.learning.billbuddy.models.Chat;

import java.util.List;

public interface ChatCallback {
    void onCallback(List<Chat> chats);
}
