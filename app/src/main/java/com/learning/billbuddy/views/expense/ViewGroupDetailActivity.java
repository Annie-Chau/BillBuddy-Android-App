package com.learning.billbuddy.views.expense;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.learning.billbuddy.ChatBoxActivity;
import com.learning.billbuddy.EditGroupInfoActivity;
import com.learning.billbuddy.R;
import com.learning.billbuddy.adapters.BalanceListAdapter;
import com.learning.billbuddy.adapters.ExpenseAdapter;
import com.learning.billbuddy.models.Expense;
import com.learning.billbuddy.models.Group;
import com.learning.billbuddy.models.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ViewGroupDetailActivity extends AppCompatActivity {

    private RadioGroup segmentGroup;
    private RadioButton rbExpense, rbBalance;
    private TextView groupNameTextView, balanceTextView, balanceAmountTextView, viewReimbursementTextView;
    private ImageView groupImageView, balanceThumbIcon;
    private FloatingActionButton chatButton;
    private Group currentGroup;
    private ImageButton updateGroupInfoButton;
    private RecyclerView expenseRecyclerView;
    private ExpenseAdapter expenseAdapter;
    private LinearLayout viewReimbursement, balanceTotalBackground;
    private List<Expense> expenseList = new ArrayList<>();
    private RecyclerView balanceListRecyclerView;
    private BalanceListAdapter balanceListAdapter;
    private EditText searchExpense;
    private FirebaseFirestore db;
    private ListenerRegistration groupListenerRegistration; // if I want to remove in onDestroy(), this one will be used

    @SuppressLint({"NotifyDataSetChanged", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setNavigationBarColor(getResources().getColor(R.color.background));
        setContentView(R.layout.activity_view_group_detail);

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
        updateGroupInfoButton = findViewById(R.id.edit_group_button);
        viewReimbursementTextView = findViewById(R.id.view_all_suggested_reimbursements_text_view);
        searchExpense = findViewById(R.id.search_expense);
        db = FirebaseFirestore.getInstance();

        balanceListRecyclerView = findViewById(R.id.balance_list_recycler_view);
        balanceListRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Retrieve data from Intent
        currentGroup = (Group) getIntent().getSerializableExtra("group");

        rbExpense.setChecked(true);
        findViewById(R.id.expense_content).setVisibility(View.VISIBLE);
        findViewById(R.id.balance_content).setVisibility(View.GONE);

        User.fetchAllUsers(users -> {
            users.stream()
                    .filter(user -> Objects.equals(user.getUserID(), Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()))
                    .findFirst()
                    .ifPresent(user -> findViewById(R.id.premium_text)
                            .setVisibility(user.isPremium() ? View.VISIBLE : View.GONE));
        });

        currentGroup.getReimbursements(this::handleDisplayBalance);

        // Set data to views
        groupNameTextView.setText(currentGroup.getName());

        if (currentGroup.getAvatarURL() != null && !currentGroup.getAvatarURL().isEmpty()) {
            Glide.with(this).load(currentGroup.getAvatarURL()).into(groupImageView);
        }

        // Set up segment button click listener
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


        updateGroupReimbursements();

        // Set onClick for viewReimbursement
        viewReimbursement.setOnClickListener(v -> {
            ViewReimbursementDetail bottomSheet = new ViewReimbursementDetail();
            currentGroup.getReimbursements(reimbursements -> {
                Double amount = getBalanceAmount(reimbursements);
                Log.d("amount", String.valueOf(amount));
                Bundle args = new Bundle();
                args.putSerializable("group", currentGroup);
                args.putDouble("amount", amount);
                bottomSheet.setArguments(args);
                bottomSheet.show(ViewGroupDetailActivity.this.getSupportFragmentManager(), "ViewReimbursementDetail");
            });
        });

        updateGroupInfoButton.setOnClickListener(v -> navigateToEditGroupInfo());

        // Handle edit group info button
        setupGroupListener();
    }

    private void navigateToEditGroupInfo() {
        Intent intent = new Intent(ViewGroupDetailActivity.this, EditGroupInfoActivity.class);
        intent.putExtra("group", currentGroup);
        startActivity(intent);

        handleSearchGroupEditText();
        handleUpdateExpenseRealTime();
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


    @SuppressLint({"DefaultLocale", "SetTextI18n", "UseCompatLoadingForDrawables"})
    private void handleDisplayBalance(List<Group.Reimbursement> reimbursements) {
        Double amount = getBalanceAmount(reimbursements);
        if (amount > 0) {
            balanceTextView.setText("You are owed");
            balanceTextView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            balanceAmountTextView.setText("VND " + String.format("%.2f", amount));
            balanceTotalBackground.setBackground(getResources().getDrawable(R.drawable.rounded_green_background));
            balanceThumbIcon.setImageDrawable(getResources().getDrawable(R.drawable.thumb_up_icon));
            balanceAmountTextView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        } else if (Math.round(amount) < 0) {
            balanceTextView.setText("You owe others");
            balanceTextView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            balanceAmountTextView.setText("VND " + String.format("%.2f", Math.abs(amount)));
            balanceTotalBackground.setBackground(getResources().getDrawable(R.drawable.rounded_red_background));
            balanceThumbIcon.setImageDrawable(getResources().getDrawable(R.drawable.thumb_down_icon));
            balanceAmountTextView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        } else {
            balanceTextView.setText("You are all settled");
            balanceTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            balanceTotalBackground.setBackground(getResources().getDrawable(R.drawable.round_gray));
            balanceAmountTextView.setText("VND 0.00");
            balanceAmountTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            balanceThumbIcon.setVisibility(View.GONE);
        }
    }

    private Double getBalanceAmount(List<Group.Reimbursement> reimbursements) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Double amount = 0.0;
        for (Group.Reimbursement reimbursement : reimbursements) {
            if (reimbursement.getPayeeId().equals(Objects.requireNonNull(currentUser).getUid())) {
                amount -= reimbursement.getAmount();
            } else if (reimbursement.getPayerId().equals(currentUser.getUid())) {
                amount += reimbursement.getAmount();
            }
        }

        return amount;
    }

    private void setupGroupListener() {
        DocumentReference groupRef = db.collection("groups")
                .document(currentGroup.getGroupID());

        // Keep the registration so we can remove the listener if needed
        groupListenerRegistration = groupRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.w("ViewGroupDetail", "Listen failed.", e);
                return;
            }
            if (snapshot != null && snapshot.exists()) {
                Group updatedGroup = snapshot.toObject(Group.class);
                if (updatedGroup != null) {
                    currentGroup = updatedGroup;
                    Log.d("ViewGroupDetail", "Real-time group update: " + currentGroup.getName());
                    updateUIWithGroupData();
                }
            }
        });
    }

    private void updateUIWithGroupData() {
        // Update group name and avatar
        groupNameTextView.setText(currentGroup.getName());
        if (currentGroup.getAvatarURL() != null && !currentGroup.getAvatarURL().isEmpty()) {
            Glide.with(this).load(currentGroup.getAvatarURL()).into(groupImageView);
        } else {
            groupImageView.setImageResource(R.drawable.example_image_1);
        }

        // Refresh the expenses in real time
        Expense.fetchAllExpenses(allExpenses -> {
            // Filter only expenses belonging to this group to fetch
            List<Expense> updatedExpenseList = allExpenses.stream()
                    .filter(expense -> currentGroup.getExpenseIDs().contains(expense.getExpenseID()))
                    .sorted((o1, o2) -> o2.getTimestamp().compareTo(o1.getTimestamp()))
                    .collect(Collectors.toList());

            // Clear + add new data
            expenseList.clear();
            expenseList.addAll(updatedExpenseList);

            expenseAdapter.notifyDataSetChanged();
            Log.d("ViewGroupDetail", "Expense list updated in real-time: " + expenseList.size() + " items");
        });

        updateGroupReimbursements();

        if(balanceListAdapter != null) {
            balanceListAdapter.notifyDataSetChanged();
        }

    }

    private void updateGroupReimbursements() {
        // Update the current group with the new data
        currentGroup.getReimbursements(reimbursements -> {
            if(balanceListAdapter == null) {
                balanceListAdapter = new BalanceListAdapter(this, currentGroup, reimbursements);
                balanceListRecyclerView.setAdapter(balanceListAdapter);
            } else {
                balanceListAdapter.updateReimbursements(reimbursements);
            }

            handleDisplayBalance(reimbursements);
        });

    }

}