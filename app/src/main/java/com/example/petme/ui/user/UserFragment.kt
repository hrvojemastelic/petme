package com.example.petme.ui.user

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
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

        // Observe authentication state
        authViewModel.authState.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                is AuthViewModel.AuthState.Authenticated -> {
                    binding.loginForm.visibility = View.GONE
                    binding.btnSignUp.visibility = View.GONE

                    binding.logoutButton.visibility = View.VISIBLE
                    binding.myads.visibility = View.VISIBLE
                    binding.settings.visibility = View.VISIBLE
                    binding.addad.visibility = View.VISIBLE
                }
                is AuthViewModel.AuthState.Unauthenticated -> {
                    binding.loginForm.visibility = View.VISIBLE
                    binding.btnSignUp.visibility = View.VISIBLE

                    binding.logoutButton.visibility = View.GONE
                    binding.myads.visibility = View.GONE
                    binding.settings.visibility = View.GONE
                    binding.addad.visibility = View.GONE
                }
                is AuthViewModel.AuthState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
            }
        })

        // Set up login button
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            authViewModel.login(email, password)
        }

        // Set up logout button
        binding.logoutButton.setOnClickListener {
            authViewModel.logout()
        }
        binding.myads.setOnClickListener {
            val intent = Intent(requireContext(), MyAdsActivity::class.java)
            startActivity(intent)
        }

        binding.btnSignUp.setOnClickListener {
            val intent = Intent(requireContext(), SignUpActivity::class.java)
            startActivity(intent)
        }
        binding.addad.setOnClickListener {
            val intent = Intent(requireContext(), AddAdActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        // Check the authentication state on fragment start
        authViewModel.checkAuthState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
