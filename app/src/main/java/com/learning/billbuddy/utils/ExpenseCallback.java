package com.learning.billbuddy.utils;

import com.learning.billbuddy.models.Expense;

import java.util.List;

public interface ExpenseCallback {
    void onCallback(List<Expense> expenses);
}