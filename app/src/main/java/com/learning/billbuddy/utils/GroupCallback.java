package com.learning.billbuddy.utils;

import com.learning.billbuddy.models.Group;

import java.util.List;

public interface GroupCallback {
    void onCallback(List<Group> groups);
}