package com.learning.billbuddy.utils;

import com.learning.billbuddy.models.User;

import java.util.List;

public interface UserCallback {
    void onCallback(List<User> users);
}
