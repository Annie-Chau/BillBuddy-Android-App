package com.learning.billbuddy.views.expense;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.learning.billbuddy.R;
import com.learning.billbuddy.adapters.ReimbursementAdapter;
import com.learning.billbuddy.models.Group;
import com.learning.billbuddy.models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ViewReimbursementDetail extends BottomSheetDialogFragment {

    private ReimbursementAdapter reimbursementAdapter;

    private RecyclerView recyclerView;

    private TextView closeButton;

    private TextView totalAmountOwed;

    private TextView balanceTotalBackground;

    private TextView headingTextView;

    public ViewReimbursementDetail() {
        // Required empty public constructor
    }

    private ArrayList<Group.Reimbursement> reimbursementArrayList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_view_imbursement, container, false);
        Group group = (Group) getArguments().getSerializable("group");

        totalAmountOwed = view.findViewById(R.id.reimbursement_amount);
        headingTextView = view.findViewById(R.id.reimbursement_form_heading_top);
        closeButton = view.findViewById(R.id.view_reimbursement_close_button);
        recyclerView = view.findViewById(R.id.reimbursement_recycler_view);
        balanceTotalBackground = view.findViewById(R.id.reimbursement_amount);


        User.getUsersBydIds(Objects.requireNonNull(group.getMemberIDs()), new User.IUsersCallBack() {
            @Override
            public void onSuccess(ArrayList<User> users) {
                Map<String, String> userMap = new HashMap<>();
                for (User user : users) {
                    userMap.put(user.getUserID(), user.getName());
                }

                reimbursementAdapter = new ReimbursementAdapter(getContext(), group, reimbursementArrayList, userMap);
                recyclerView = view.findViewById(R.id.reimbursement_recycler_view);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                recyclerView.setAdapter(reimbursementAdapter);
                updateReimbursement(group);
                handleDisplayBalance(reimbursementArrayList);
            }

            @Override
            public void onFailure(String error) {

            }
        });

        closeButton.setOnClickListener(v -> dismiss());
        return view;
    }

    private void updateReimbursement(Group group) {
        group.getReimbursements(reimbursements -> {
            Log.d("ViewReimbursementDetail", "Reimbursements: " + reimbursements.toString());
            Log.d("ViewReimbursementDetail", "Refetch");
            //sort the reimbursement pirotize the one that are relate to current payerId or payeeId is currentuser
            reimbursements = reimbursements.stream().sorted((r1, r2) -> {
                if (r1.getPayeeId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    return -1;
                } else if (r2.getPayeeId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    return 1;
                } else {
                    return 0;
                }
            }).collect(Collectors.toList());

            reimbursementAdapter.reimbursementList.clear();
            reimbursementAdapter.reimbursementList.addAll(reimbursements);
            reimbursementAdapter.notifyDataSetChanged();
        });
    }

    private void handleDisplayBalance(List<Group.Reimbursement> reimbursements) {
        Double amount = getBalanceAmount(reimbursements);
        if (amount > 0) {
            balanceTotalBackground.setText("đ" + String.format("%.3f", amount));
            balanceTotalBackground.setBackground(getResources().getDrawable(R.drawable.rounded_green_background));
        } else if (amount < 0) {
            totalAmountOwed.setText("đ" + String.format("%.3f", amount));
            balanceTotalBackground.setBackground(getResources().getDrawable(R.drawable.rounded_red_background));
        } else {
            balanceTotalBackground.setBackground(getResources().getDrawable(R.drawable.round_gray));
            totalAmountOwed.setText("đ0.00");
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
