package com.learning.billbuddy.views.expense;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
    private EditText searchExpense;
    private LinearLayout viewReimbursement;
    private TextView viewReimbursementTextView;

    @SuppressLint({"NotifyDataSetChanged", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setNavigationBarColor(getResources().getColor(R.color.background));
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
        viewReimbursementTextView = findViewById(R.id.view_all_suggested_reimbursements_text_view);
        searchExpense = findViewById(R.id.search_expense);


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
                searchExpense.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.rb_balance) {
                findViewById(R.id.expense_content).setVisibility(View.GONE);
                findViewById(R.id.balance_content).setVisibility(View.VISIBLE);
                searchExpense.setVisibility(View.GONE);
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


        viewReimbursement.setOnTouchListener((view, motionEvent) -> {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // Handle touch (press)
                    viewReimbursementTextView.setBackgroundColor(getColor(R.color.profile_page_gray));
                    viewReimbursement.setBackgroundColor(getColor(R.color.profile_page_gray));
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // Handle release
                    viewReimbursementTextView.setBackgroundColor(getColor(R.color.white));
                    viewReimbursement.setBackgroundColor(getColor(R.color.white)); // Reset color or set a new one
                    break;
            }
            return false;
        });


        viewReimbursement.setOnClickListener(v -> {
            // Handle click on the card
            Log.d("GroupAdapter", "Clicked on group: " + currentGroup.getName());
            ViewReimbursementDetail bottomSheet = new ViewReimbursementDetail();
            Bundle args = new Bundle();
            args.putSerializable("group", currentGroup);
            bottomSheet.setArguments(args);
            bottomSheet.show(ViewGroupDetailActivity.this.getSupportFragmentManager(), "ViewReimbursementDetail");
        });

        handleSearchGroupEditText();
        handleUpdateExpenseRealTime();
        handleUpdateAccountBalanceList();
    }

    private void handleSearchGroupEditText() {
        searchExpense.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not needed
            }

            @Override
            public void afterTextChanged(Editable s) {
                onSearch();
            }
        });
    }

    private void handleUpdateAccountBalanceList() {
        currentGroup.getReimbursements(reimbursements -> {
            // This code will be executed when the results are available
            Log.d("Test", reimbursements.toString());
            balanceListAdapter = new BalanceListAdapter(this, currentGroup, reimbursements);
            balanceListRecyclerView.setAdapter(balanceListAdapter);
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void onSearch() {
        String searchQuery = searchExpense.getText().toString().toLowerCase();
        Log.d("HomePage", "before serach y: " + expenseList.size());

        if (searchQuery.trim().isEmpty()) {
            expenseAdapter.expenseList = expenseList;
        } else {
            expenseAdapter.expenseList = expenseList.stream()
                    .filter(group -> group.isQualifyForSearch(searchQuery))
                    .collect(Collectors.toList());
        }
        expenseAdapter.notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
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
                                    //sort by timestamp first if equal sort by id in descending order
                                    .sorted((e1, e2) -> {
                                        if (e1.getTimestamp().compareTo(e2.getTimestamp()) == 0) {
                                            return e2.getExpenseID().compareTo(e1.getExpenseID());
                                        }
                                        return e2.getTimestamp().compareTo(e1.getTimestamp());
                                    })

                                    .collect(Collectors.toList());

                            Log.d("Expense", expenses.toString());
                            Log.d("Expense Change", expenseList.toString());

                            if (expenseAdapter.expenseList.isEmpty()) {

                                expenseAdapter.expenseList.addAll(expenseList);
                                expenseAdapter.notifyDataSetChanged();

                            } else if (expenseAdapter.getItemCount() < expenseList.size()) {

                                for (int i = 0; i < expenseList.size(); i++) {
                                    if (i >= expenseAdapter.expenseList.size()) {
                                        expenseAdapter.expenseList.add(expenseList.get(i));
                                        expenseRecyclerView.scrollToPosition(i - 1);
                                        expenseAdapter.notifyItemInserted(i);

                                    } else if (!expenseAdapter.expenseList.get(i).getExpenseID().equals(expenseList.get(i).getExpenseID())) {
                                        expenseAdapter.expenseList.add(i, expenseList.get(i));
                                        expenseRecyclerView.scrollToPosition(i);
                                        expenseAdapter.notifyItemInserted(i);
                                        if (i + 1 < expenseAdapter.expenseList.size()) {
                                            expenseAdapter.notifyItemChanged(i + 1);
                                        }
                                    }
                                }

                            } else {
                                expenseAdapter.expenseList.clear();
                                expenseAdapter.expenseList.addAll(expenseList);
                                expenseAdapter.notifyDataSetChanged();
                            }
                            handleUpdateAccountBalanceList();
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
            balanceTextView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            balanceAmountTextView.setText("đ" + String.format("%.3f", amount));
            balanceTotalBackground.setBackground(getResources().getDrawable(R.drawable.rounded_green_background));
            balanceThumbIcon.setImageDrawable(getResources().getDrawable(R.drawable.thumb_up_icon));
            balanceAmountTextView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        } else if (Math.round(amount) < 0) {
            balanceTextView.setText("You owe others");
            balanceTextView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            balanceAmountTextView.setText("đ" + String.format("%.3f", amount));
            balanceTotalBackground.setBackground(getResources().getDrawable(R.drawable.rounded_red_background));
            balanceThumbIcon.setImageDrawable(getResources().getDrawable(R.drawable.thumb_down_icon));
            balanceAmountTextView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        } else {
            balanceTextView.setText("You are all settled");
            balanceTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            balanceTotalBackground.setBackground(getResources().getDrawable(R.drawable.round_gray));
            balanceAmountTextView.setText("đ0.00");
            balanceAmountTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
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