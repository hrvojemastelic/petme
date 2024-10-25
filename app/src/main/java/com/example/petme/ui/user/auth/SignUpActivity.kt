package com.example.petme.ui.user.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.petme.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Handle private account selection
        binding.privateButton.setOnClickListener {
            setupAccountTypeUI("private")
        }

        // Handle business account selection
        binding.businessButton.setOnClickListener {
            setupAccountTypeUI("business")
        }

        // Handle sign-up
        binding.signUpButton.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()
            val confirmPassword = binding.confirmPasswordInput.text.toString().trim()
            val address = binding.addressInput.text.toString().trim()
            val phone = binding.phoneInput.text.toString().trim()
            val accountType = binding.selectAccountTypeText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && confirmPassword == password && address.isNotEmpty() && phone.isNotEmpty()) {
                signUpUser(email, password, address, phone, accountType)
            } else {
                Toast.makeText(this, "Please fill in all fields correctly", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupAccountTypeUI(accountType: String) {
        // Hide account type selection and show the form
        binding.accountTypeSelectionLayout.visibility = View.GONE
        binding.accountDetailsFormLayout.visibility = View.VISIBLE

        // Set account type
        binding.selectAccountTypeText.text = accountType
    }

    private fun signUpUser(email: String, password: String, address: String, phone: String, accountType: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    val dateCreated = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                    // Create user data map
                    val userData = hashMapOf(
                        "email" to email,
                        "address" to address,
                        "phone" to phone,
                        "accountType" to accountType,
                        "dateCreated" to dateCreated
                    )

                    // Save user data to Firestore
                    userId?.let {
                        db.collection("users").document(it).set(userData)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Failed to save user data: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "Sign-up failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
