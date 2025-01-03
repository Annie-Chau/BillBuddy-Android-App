package com.learning.billbuddy.composables.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.example.blooddonation.ui.theme.AppTheme
import com.learning.billbuddy.composables.authentications.Authentication

class AuthenticationFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                AppTheme {
                    Authentication(
                        modifier = Modifier
                            .background(AppTheme.colorScheme.background)
                            .windowInsetsPadding(WindowInsets.statusBars)
                    )
                }
            }
        }
    }
}