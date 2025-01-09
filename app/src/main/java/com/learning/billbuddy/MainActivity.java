package com.learning.billbuddy;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.learning.billbuddy.models.User;
import com.learning.billbuddy.views.authentication.Login;
import com.learning.billbuddy.views.home.HomePage;
import com.learning.billbuddy.views.profile.Profile;

public class MainActivity extends AppCompatActivity {

    FirebaseUser currentUser;
    User currentUserData;
    FirebaseAuth firebaseAuth;

    private final NavigationBarView.OnItemSelectedListener navListener =
            item -> {
                int itemId = item.getItemId();
                Fragment selectedFragment = null;

                if (itemId == R.id.bottom_navigation_home) {
                    selectedFragment = new HomePage();
//                    Bundle bundle = new Bundle();
//                    bundle.putSerializable("user", currentUser);
//                    selectedFragment.setArguments(bundle);
                } else if (itemId == R.id.bottom_navigation_profile) {
                    selectedFragment = new Profile();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("user", currentUserData);
                    selectedFragment.setArguments(bundle);
                } else { // default
                    selectedFragment = new HomePage();
//                    Bundle bundle = new Bundle();
//                    bundle.putSerializable("user", currentUser);
//                    selectedFragment.setArguments(bundle);
                }

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();

                return true;
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setNavigationBarColor(getColor(R.color.bottom_navigation));
        super.onCreate(savedInstanceState);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Check if user is logged in
        currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            // Redirect to LoginActivity if not logged in
            Intent intent = new Intent(MainActivity.this, Login.class);
            startActivity(intent);
            finish();
        } else {
            // User is logged in, proceed with fetching user data or other initialization
            setContentView(R.layout.activity_main);
            User.fetchAllUsers(users -> {
                for (User user: users){
                    if (user.getUserID().equals(currentUser.getUid())){
                        currentUserData = user;
                    }
                }
            });
            BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
            bottomNav.setSelectedItemId(R.id.bottom_navigation_home);
            bottomNav.setOnItemSelectedListener(navListener);
            Fragment selectedFragment = new HomePage();

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    selectedFragment).commit();
        }

    }

    // Example for fetching users
    private void fetchUsers() {
        User.fetchAllUsers(users -> {
            Log.d("User", users.toString());
        });
    }
}
