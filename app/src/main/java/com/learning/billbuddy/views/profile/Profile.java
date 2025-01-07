package com.learning.billbuddy.views.profile;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.learning.billbuddy.AddUserInfo;
import com.learning.billbuddy.R;
import com.learning.billbuddy.models.User;
import com.learning.billbuddy.views.authentication.Login;

import java.util.Objects;

public class Profile extends Fragment {


    private Image profileImage;
    private TextView userName;
    private TextView phoneNumber;

    private FirebaseFirestore db;

    private ImageButton editProfileButton;
    User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_profile, container, false);
        db = FirebaseFirestore.getInstance();

        Button logoutButton = view.findViewById(R.id.profile_logout_button);
        logoutButton.setOnClickListener(v -> logout());

        FirebaseUser userAuth = FirebaseAuth.getInstance().getCurrentUser();

        if (userAuth == null) {
            return view;
        }

        userName = view.findViewById(R.id.profile_userName_textField);
        phoneNumber = view.findViewById(R.id.profile_phoneNumber_textField);
        assert getArguments() != null;
        currentUser = (User) getArguments().getSerializable("user");

        userName.setText(Objects.requireNonNull(currentUser).getName());
        phoneNumber.setText(currentUser.getPhoneNumber());

        editProfileButton = view.findViewById(R.id.profile_edit_button);
        editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), AddUserInfo.class);
            intent.putExtra("isEdit", true);
            intent.putExtra("name", userName.getText().toString());
            intent.putExtra("phoneNumber", phoneNumber.getText().toString());
            startActivity(intent);
        });

        return view;
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getActivity(), Login.class);
        startActivity(intent);
        requireActivity().finish();
    }
}