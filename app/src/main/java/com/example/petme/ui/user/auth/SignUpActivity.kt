package com.example.petme.ui.user.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.petme.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var viewModel: SignUpViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        viewModel = SignUpViewModel()

        // Account type selection
        binding.privateButton.setOnClickListener {
            setupAccountTypeUI("private")
        }
        binding.businessButton.setOnClickListener {
            setupAccountTypeUI("business")
        }

        // Sign-up button
        binding.signUpButton.setOnClickListener {
            val username = binding.usernameInput.text.toString().trim()
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()
            val confirmPassword = binding.confirmPasswordInput.text.toString().trim()
            val address = binding.addressInput.text.toString().trim()
            val phone = binding.phoneInput.text.toString().trim()
            val accountType = binding.selectAccountTypeText.text.toString()

            if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() &&
                confirmPassword == password && address.isNotEmpty() && phone.isNotEmpty()
            ) {
                checkUsernameAndEmail(username, email, password, address, phone, accountType)
            } else {
                Toast.makeText(this, "Please fill in all fields correctly", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupAccountTypeUI(accountType: String) {
        binding.accountTypeSelectionLayout.visibility = View.GONE
        binding.accountDetailsFormLayout.visibility = View.VISIBLE
        binding.selectAccountTypeText.text = accountType
    }

    private fun checkUsernameAndEmail(username: String, email: String, password: String, address: String, phone: String, accountType: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.accountDetailsFormLayout.visibility = View.GONE

        db.collection("users").whereEqualTo("username", username).get()
            .addOnSuccessListener { usernameDocs ->
                if (!usernameDocs.isEmpty) {
                    showFormWithMessage("Username already exists")
                } else {
                    db.collection("users").whereEqualTo("email", email).get()
                        .addOnSuccessListener { emailDocs ->
                            if (!emailDocs.isEmpty) {
                                showFormWithMessage("Email already exists")
                            } else {
                                viewModel.signUpUser(
                                    email, password, accountType, phone, address, username,
                                    onSuccess = {
                                        Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                                        finish()  // Close activity
                                    },
                                    onFailure = { e ->
                                        showFormWithMessage("Sign-up failed: ${e.message}")
                                    }
                                )
                            }
                        }
                        .addOnFailureListener { e ->
                            showFormWithMessage("Error checking email: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                showFormWithMessage("Error checking username: ${e.message}")
            }
    }

    private fun showFormWithMessage(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.accountDetailsFormLayout.visibility = View.VISIBLE
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
