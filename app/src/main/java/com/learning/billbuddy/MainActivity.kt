package com.learning.billbuddy

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.compose.AndroidFragment
import com.example.blooddonation.ui.theme.AppTheme
import com.learning.billbuddy.composables.authentications.Authentication
import com.learning.billbuddy.composables.fragments.AuthenticationFragment


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AndroidFragment(AuthenticationFragment::class.java)
        }
    }
}