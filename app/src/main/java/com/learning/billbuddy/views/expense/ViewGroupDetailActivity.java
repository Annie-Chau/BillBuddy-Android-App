package com.learning.billbuddy.views.expense;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.learning.billbuddy.ChatBoxActivity;
import com.learning.billbuddy.R;
import com.learning.billbuddy.adapters.BalanceListAdapter;
import com.learning.billbuddy.adapters.ExpenseAdapter;
import com.learning.billbuddy.models.Expense;
import com.learning.billbuddy.models.Group;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ViewGroupDetailActivity extends AppCompatActivity {

    private RadioGroup segmentGroup;
    private RadioButton rbExpense, rbBalance;
    private TextView groupNameTextView, balanceTextView, balanceAmountTextView;
    private ImageView groupImageView, balanceThumbIcon;
    private FloatingActionButton chatButton;
    private Group currentGroup;
    private RecyclerView expenseRecyclerView;
    private ExpenseAdapter expenseAdapter;
    private LinearLayout balanceTotalBackground;
    private List<Expense> expenseList = new ArrayList<>();

    private RecyclerView balanceListRecyclerView;
    private BalanceListAdapter balanceListAdapter;


    private TextView viewReimbursement;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_balance_of_group);

        // Initialize UI elements
        segmentGroup = findViewById(R.id.segment_button_group);
        rbExpense = findViewById(R.id.rb_expense);
        rbBalance = findViewById(R.id.rb_balance);
        groupNameTextView = findViewById(R.id.group_name);
        groupImageView = findViewById(R.id.group_image);
        chatButton = findViewById(R.id.chat_button);
        expenseRecyclerView = findViewById(R.id.expense_list);
        expenseRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        expenseAdapter = new ExpenseAdapter(this, expenseList);
        expenseRecyclerView.setAdapter(expenseAdapter);
        balanceTextView = findViewById(R.id.balance_total_text);
        balanceAmountTextView = findViewById(R.id.balance_total_amount_text);
        balanceThumbIcon = findViewById(R.id.balance_total_thumb_icon);
        balanceTotalBackground = findViewById(R.id.balance_total_background);
        viewReimbursement = findViewById(R.id.view_all_suggested_reimbursements);


        balanceListRecyclerView = findViewById(R.id.balance_list_recycler_view);
        balanceListRecyclerView.setLayoutManager(new LinearLayoutManager(this));


        // Retrieve data from Intent
        currentGroup = (Group) getIntent().getSerializableExtra("group");

        rbExpense.setChecked(true);
        findViewById(R.id.expense_content).setVisibility(View.VISIBLE);
        findViewById(R.id.balance_content).setVisibility(View.GONE);


        currentGroup.getReimbursements(reimbursements -> {
            handleDisplayBalance(reimbursements);
        });

        // Set data to views
        groupNameTextView.setText(currentGroup.getName());

        if (currentGroup.getAvatarURL() != null && !currentGroup.getAvatarURL().isEmpty()) {
            Glide.with(this).load(currentGroup.getAvatarURL()).into(groupImageView);
        }


        segmentGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_expense) {
                findViewById(R.id.expense_content).setVisibility(View.VISIBLE);
                findViewById(R.id.balance_content).setVisibility(View.GONE);
            } else if (checkedId == R.id.rb_balance) {
                findViewById(R.id.expense_content).setVisibility(View.GONE);
                findViewById(R.id.balance_content).setVisibility(View.VISIBLE);
            }
        });

        // Set up chat button click listener
        chatButton.setOnClickListener(v -> navigateToChatBox());

        findViewById(R.id.return_button).setOnClickListener(v -> finish());

        findViewById(R.id.to_add_expense_btn).setOnClickListener(v -> {
            AddExpenseBottomSheet bottomSheet = new AddExpenseBottomSheet();
            Bundle args = new Bundle();
            args.putSerializable("group", currentGroup);
            bottomSheet.setArguments(args);
            bottomSheet.show(ViewGroupDetailActivity.this.getSupportFragmentManager(), "AddGroupBottomSheetDialog");
        });

//        currentGroup.getReimbursements(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(), reimbursements -> {
//            // This code will be executed when the results are available
////            Log.d("Test", reimbursements.());
//        });

        currentGroup.getReimbursements(reimbursements -> {
            // This code will be executed when the results are available
            Log.d("Test", reimbursements.toString());
            balanceListAdapter = new BalanceListAdapter(this, currentGroup, reimbursements);
            balanceListRecyclerView.setAdapter(balanceListAdapter);
        });

        viewReimbursement.setOnClickListener(v -> {
            ViewReimbursementDetail bottomSheet = new ViewReimbursementDetail();
            Bundle args = new Bundle();
            args.putSerializable("group", currentGroup);
            bottomSheet.setArguments(args);
            bottomSheet.show(ViewGroupDetailActivity.this.getSupportFragmentManager(), "ViewReimbursementDetail");
        });
        handleUpdateExpenseRealTime();
    }

    private void handleUpdateExpenseRealTime() {
        Expense.fetchAllExpenses(expenses -> {

            FirebaseFirestore.getInstance()
                    .collection("groups")
                    .document(currentGroup.getGroupID())
                    .addSnapshotListener((documentSnapshot, e) -> {

                        if (e != null) {
                            return;
                        }

                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            currentGroup = documentSnapshot.toObject(Group.class);
                            expenseList = expenses.stream()
                                    .filter(expense ->
                                            Objects.requireNonNull(currentGroup)
                                                    .getExpenseIDs()
                                                    .contains(expense.getExpenseID()))
                                    .sorted((o1, o2) -> o2.getTimestamp().compareTo(o1.getTimestamp()))
                                    .collect(Collectors.toList());

                            Log.d("Expense", expenses.toString());
                            Log.d("Expense Change", expenseList.toString());

                            if (expenseAdapter.expenseList.isEmpty()) {
                                expenseAdapter.expenseList.addAll(expenseList);
                                expenseAdapter.notifyDataSetChanged();
                            } else if (expenseAdapter.getItemCount() < expenseList.size()) {
                                expenseAdapter.expenseList.add(0, expenseList.get(0));
                                expenseAdapter.notifyItemInserted(0);
                                expenseAdapter.notifyItemChanged(1);
                                expenseRecyclerView.scrollToPosition(0);
                            } else {
                                return;
                            }
                        }
                    });
        });
    }


    private void navigateToChatBox() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userID = currentUser.getUid();
            Intent intent = new Intent(ViewGroupDetailActivity.this, ChatBoxActivity.class);
            intent.putExtra("group", currentGroup);
            intent.putExtra("USER_ID", userID);
            startActivity(intent);
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }


    private void handleDisplayBalance(List<Group.Reimbursement> reimbursements) {
        Double amount = getBalanceAmount(reimbursements);
        if (amount > 0) {
            balanceTextView.setText("You are owed");
            balanceAmountTextView.setText("đ" + String.format("%.3f", amount));
            balanceTotalBackground.setBackground(getResources().getDrawable(R.drawable.rounded_green_background));
            balanceThumbIcon.setImageDrawable(getResources().getDrawable(R.drawable.thumb_up_icon));
        } else if (amount < 0) {
            balanceTextView.setText("You owe others");
            balanceAmountTextView.setText("đ" + String.format("%.3f", amount));
            balanceTotalBackground.setBackground(getResources().getDrawable(R.drawable.rounded_red_background));
            balanceThumbIcon.setImageDrawable(getResources().getDrawable(R.drawable.thumb_down_icon));
        } else {
            balanceTextView.setText("You are all settled");
            balanceTotalBackground.setBackground(getResources().getDrawable(R.drawable.round_gray));
            balanceAmountTextView.setText("đ0.00");
            balanceThumbIcon.setVisibility(View.GONE);
        }
    }

    private Double getBalanceAmount(List<Group.Reimbursement> reimbursements) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Double amount = 0.0;
        for (Group.Reimbursement reimbursement : reimbursements) {
            if (reimbursement.getPayeeId().equals(currentUser.getUid())) {
                amount -= reimbursement.getAmount();
            } else if (reimbursement.getPayerId().equals(currentUser.getUid())) {
                amount += reimbursement.getAmount();
            }
        }

        return amount;
        // Logic to calculate the amount owed to currentLogin user
    }

}