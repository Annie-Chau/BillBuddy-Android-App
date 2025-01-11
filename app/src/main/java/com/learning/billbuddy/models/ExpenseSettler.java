package com.learning.billbuddy.models;


import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpenseSettler {
    // 1. Method to get reimbursements

    public static ArrayList<Group.Reimbursement> getReimbursement(Map<String, ArrayList<Double>> debtHashHistory) {
//        // List to store the reimbursements
        ArrayList<Group.Reimbursement> reimbursements = new ArrayList<>();
        ArrayList<String> users = new ArrayList<>(debtHashHistory.keySet());

        ArrayList<ArrayList<Double>> totalDebt = new ArrayList<>();
        for (Map.Entry<String, ArrayList<Double>> entry : debtHashHistory.entrySet()) {
            totalDebt.add(entry.getValue());
        }
//
//        // Total credit initialization (matrix with zeros)
        ArrayList<ArrayList<Double>> totalCredit = new ArrayList<>();
        for (Map.Entry<String, ArrayList<Double>> entry : debtHashHistory.entrySet()) {
            totalCredit.add(new ArrayList<>(Collections.nCopies(entry.getValue().size(), 0.0D)));
        }

        for (int i = 0; i < totalDebt.size(); i++) {
            ArrayList<Double> debt = totalDebt.get(i);
            for (int j = 0; j < debt.size(); j++) {
                totalCredit.get(j).set(i, debt.get(j) + totalCredit.get(j).get(i));
            }
        }

        ArrayList<Double> totalAmount = new ArrayList<>();
        for (int i = 0; i < debtHashHistory.size(); i++) {
            totalAmount.add(computeTotalAmount(totalDebt.get(i), totalCredit.get(i)));
        }

        ExpenseSettler.payRecursive(totalAmount, users, reimbursements);

        return reimbursements;
    }

    // Compute the total amount (debt - credit)
    private static double computeTotalAmount(ArrayList<Double> debt, ArrayList<Double> credit) {
        double debtSum = debt.stream().mapToDouble(Double::doubleValue).sum();
        double creditSum = credit.stream().mapToDouble(Double::doubleValue).sum();
        return debtSum - creditSum;
    }

    // Recursive method to process payments and calculate reimbursements
    private static void payRecursive(ArrayList<Double> amount, ArrayList<String> users, ArrayList<Group.Reimbursement> reimbursements) {
        // Find the maximum debt and minimum credit
        double maxDebt = Collections.max(amount);
        double maxCredit = Collections.min(amount);

        int maxDebitorIndex = amount.indexOf(maxDebt);
        int maxCreditorIndex = amount.indexOf(maxCredit);

        // If all debts are settled (both amounts are 0), stop the recursion
        if (amount.get(maxDebitorIndex) == 0 && amount.get(maxCreditorIndex) == 0) {
            return;
        }

        // Determine the minimum transfer amount
        double minimum = Math.min(Math.abs(amount.get(maxDebitorIndex)), Math.abs(amount.get(maxCreditorIndex)));

        // Update amounts for debitor and creditor
        amount.set(maxCreditorIndex, amount.get(maxCreditorIndex) + minimum);
        amount.set(maxDebitorIndex, amount.get(maxDebitorIndex) - minimum);

        if (minimum == 0) {
            return;
        }
        reimbursements.add(new Group.Reimbursement(users.get(maxDebitorIndex), users.get(maxCreditorIndex), minimum));

        // Recursively call the method to continue the process
        payRecursive(amount, users, reimbursements);
    }

}