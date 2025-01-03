package com.learning.billbuddy.composables.fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.blooddonation.composables.register.login_signup.Login
import com.example.blooddonation.composables.register.login_signup.Signup
import com.example.blooddonation.ui.theme.AppTheme
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.learning.billbuddy.R
import com.learning.billbuddy.composables.profile.ProfileScreen
import com.learning.billbuddy.models.compose.UserViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthenticationFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return ComposeView(requireContext()).apply {
            setContent {
                val navController = rememberNavController()
                var auth by remember { mutableStateOf(Firebase.auth.currentUser) }
                val userViewModel = viewModel<UserViewModel>()
                val launcher = rememberFirebaseAuthLauncher(onAuthComplete = {result ->
                    auth = result.user
                    userViewModel.setCurrentUser(result.user!!)
                    navController.navigate("profile")
                }, onAuthError = {

                })

                AppTheme {
                    // A surface container using the 'background' color from the theme
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                    ) {

                        NavHost(navController = navController, startDestination = "sign_in") {
                            composable("sign_in") {

                                LaunchedEffect(key1 = Unit) {
                                    if (Firebase.auth.currentUser != null) {
                                        navController.navigate("profile")
                                    }
                                }

                                Login(
                                    onNavigateToSignUpScreen = {
                                        navController.navigate("sign_up")
                                    },
                                    onLogin = { email, password ->

                                    },
                                    onSignInByGoogle = {
                                        Log.d("AuthenticationFragment", "onSignInByGoogle")
                                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                            .requestIdToken(getString(R.string.google_web_client_id))
                                            .requestEmail()
                                            .build()

                                        val googleSignInClient= GoogleSignIn.getClient(requireContext(), gso)
                                        launcher.launch(googleSignInClient.signInIntent)

                                    }
                                )
                            }
                            composable("sign_up") {
                                Signup(
                                    onSignup = { email, password ->

                                    },
                                    onNavigateSignInScreen = {
                                        navController.navigate("sign_in")
                                    }
                                )
                            }
                            composable("profile") {
                                ProfileScreen(
                                    userData = userViewModel.currentUser,
                                    onSignOut = {
                                        Firebase.auth.signOut()
                                        userViewModel.currentUser = null
                                        navController.navigate("sign_in")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun rememberFirebaseAuthLauncher(
    onAuthComplete: (AuthResult)->Unit,
    onAuthError: (ApiException)->Unit
    ): ManagedActivityResultLauncher<Intent, ActivityResult>{

    val scope = rememberCoroutineScope()

    return rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
            scope.launch {
                val authUiClient = Firebase.auth.signInWithCredential(credential).await()
                onAuthComplete(authUiClient)
            }
        }catch (e: ApiException){
            Log.e("AuthenticationFragment", "Google sign in failed", e)
            onAuthError(e)
        }
    }
}

