package com.learning.billbuddy.utils;

import com.learning.billbuddy.models.Debt;

import java.util.List;

public interface DebtCallback {
    void onCallback(List<Debt> debts);
}
