package com.example.petme.ui.user

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.petme.databinding.FragmentUserBinding
import com.example.petme.ui.ads.MyAdsActivity
import com.example.petme.ui.ads.addad.AddAdActivity
import com.example.petme.ui.user.auth.AuthViewModel
import com.example.petme.ui.user.auth.SignUpActivity

class UserFragment : Fragment() {

    private var _binding: FragmentUserBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe authentication state to manage UI updates
        authViewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthViewModel.AuthState.Authenticated -> {
                    showLoggedInUI()
                }
                is AuthViewModel.AuthState.Unauthenticated -> {
                    showLoggedOutUI()
                }
                is AuthViewModel.AuthState.Error -> {
                    binding.progressBar.visibility = View.GONE // Hide progress bar on error
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    // If login fails, show the login form
                    binding.loginForm.visibility = View.VISIBLE
                    binding.btnSignUp.visibility = View.VISIBLE
                }
            }
        }

        // Observe username changes
        authViewModel.username.observe(viewLifecycleOwner) { username ->
            binding.usernameTextView.text = username
            binding.usernameTextView.visibility = if (username.isNullOrEmpty()) View.GONE else View.VISIBLE
        }

        // Observe email changes
        authViewModel.email.observe(viewLifecycleOwner) { email ->
            binding.emailTextView.text = email
            binding.emailTextView.visibility = if (email.isNullOrEmpty()) View.GONE else View.VISIBLE
        }

        // Observe username changes
        authViewModel.phoneNumbar.observe(viewLifecycleOwner) { phoneNumbar ->
            Log.d("jebimaterviše",phoneNumbar.toString())
            binding.phoneTextView.text = phoneNumbar.toString()
            binding.phoneTextView.visibility = if (phoneNumbar.toString().isNullOrEmpty()) View.GONE else View.VISIBLE
        }
        // Observe username changes
        authViewModel.address.observe(viewLifecycleOwner) { address ->
            binding.addressTextView.text = address
            binding.addressTextView.visibility = if (address.isNullOrEmpty()) View.GONE else View.VISIBLE
        }

        // Set up login button
        binding.loginButton.setOnClickListener {
           if(binding.emailEditText.text.toString().trim().isNotEmpty() && binding.passwordEditText.text.toString().trim().isNotEmpty() )
           { val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            showLoading(true) // Show progress bar and hide other views
            authViewModel.login(email, password)

            // Clear the email and password EditText fields
            binding.emailEditText.text?.clear()
            binding.passwordEditText.text?.clear()}
        }

        // Set up logout button
        binding.logoutButton.setOnClickListener {
            binding.usernameTextView.text = null
            binding.usernameTextView.visibility = View.GONE
            binding.emailTextView.text = null
            binding.emailTextView.visibility = View.GONE
            authViewModel.logout()
        }

        binding.myads.setOnClickListener {
            startActivity(Intent(requireContext(), MyAdsActivity::class.java))
        }

        binding.btnSignUp.setOnClickListener {
            startActivity(Intent(requireContext(), SignUpActivity::class.java))
        }

        binding.addad.setOnClickListener {
            startActivity(Intent(requireContext(), AddAdActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        authViewModel.checkAuthState() // Ensure auth state is checked when fragment starts
        updateUI()
    }

    private fun showLoggedInUI() {
        binding.progressBar.visibility = View.GONE
        binding.loginForm.visibility = View.GONE
        binding.btnSignUp.visibility = View.GONE
        binding.logoutButton.visibility = View.VISIBLE
        binding.myads.visibility = View.VISIBLE
        binding.settings.visibility = View.VISIBLE
        binding.addad.visibility = View.VISIBLE
        binding.usernameTextView.visibility = View.VISIBLE
        binding.emailTextView.visibility = View.VISIBLE
        // Update UI with username and email
        updateUI()
    }

    private fun showLoggedOutUI() {
        binding.progressBar.visibility = View.GONE
        binding.loginForm.visibility = View.VISIBLE
        binding.btnSignUp.visibility = View.VISIBLE
        binding.logoutButton.visibility = View.GONE
        binding.myads.visibility = View.GONE
        binding.settings.visibility = View.GONE
        binding.addad.visibility = View.GONE
        binding.usernameTextView.visibility = View.GONE
        binding.emailTextView.visibility = View.GONE
    }

    private fun updateUI() {
        if (authViewModel.authState.value is AuthViewModel.AuthState.Authenticated) {
            binding.usernameTextView.text = authViewModel.username.value
            binding.usernameTextView.visibility = View.VISIBLE
            binding.emailTextView.text = authViewModel.email.value
            binding.emailTextView.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        if (isLoading) {
            binding.loginForm.visibility = View.GONE
            binding.btnSignUp.visibility = View.GONE
            binding.logoutButton.visibility = View.GONE
            binding.myads.visibility = View.GONE
            binding.settings.visibility = View.GONE
            binding.addad.visibility = View.GONE
            binding.usernameTextView.visibility = View.GONE
            binding.emailTextView.visibility = View.GONE
        }
    }
}
