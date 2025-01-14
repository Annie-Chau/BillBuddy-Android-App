package com.learning.billbuddy.utils;

import com.learning.billbuddy.models.User;

public interface CurrentUserCallback {
    void onCallback(User user);
}
