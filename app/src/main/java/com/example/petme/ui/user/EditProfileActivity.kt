package com.example.petme.ui.user

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.petme.R
import com.example.petme.databinding.ActivityEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        userId = auth.currentUser?.uid ?: return

        loadUserData()

        // Disable fields initially
        binding.phoneInput.isEnabled = false
        binding.passwordInput.isEnabled = false
        binding.confirmPasswordInput.isEnabled = false

        binding.editPhoneButton.setOnClickListener {
            binding.phoneInput.isEnabled = true
            binding.phoneInput.requestFocus()
        }

        binding.editPasswordButton.setOnClickListener {
            binding.passwordInput.isEnabled = true
            binding.confirmPasswordInput.isEnabled = true
            binding.passwordInput.requestFocus()
        }

        binding.saveChangesButton.setOnClickListener {
            saveChanges()
        }
    }

    private fun loadUserData() {
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    binding.usernameText.text = document.getString("username") ?: ""
                    binding.emailText.text = document.getString("email") ?: ""
                    binding.addressText.text = document.getString("address") ?: ""
                    binding.accountTypeText.text = document.getString("accountType") ?: ""
                    binding.phoneInput.setText(document.getString("phoneNumber") ?: "")
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Greška pri dohvaćanju podataka", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveChanges() {
        val newPhone = binding.phoneInput.text.toString().trim()
        val newPassword = binding.passwordInput.text.toString().trim()
        val confirmPassword = binding.confirmPasswordInput.text.toString().trim()

        // Provjera broja mobitela
        val phoneRegex = Regex("^\\+?[0-9]{7,15}\$")
        if (!phoneRegex.matches(newPhone)) {
            Toast.makeText(this, "Neispravan broj mobitela", Toast.LENGTH_SHORT).show()
            return
        }

        // Ažuriranje broja mobitela u Firestore
        val updates = hashMapOf<String, Any>("phoneNumber" to newPhone)
        firestore.collection("users").document(userId)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Broj mobitela ažuriran", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Greška pri spremanju broja", Toast.LENGTH_SHORT).show()
            }

        // Ažuriranje lozinke ako su unesena oba polja
        if (newPassword.isNotEmpty() || confirmPassword.isNotEmpty()) {
            if (newPassword.length < 6) {
                Toast.makeText(this, "Lozinka mora imati barem 6 znakova", Toast.LENGTH_SHORT).show()
                return
            }

            if (newPassword != confirmPassword) {
                Toast.makeText(this, "Lozinke se ne podudaraju", Toast.LENGTH_SHORT).show()
                return
            }

            auth.currentUser?.updatePassword(newPassword)
                ?.addOnSuccessListener {
                    Toast.makeText(this, "Lozinka ažurirana", Toast.LENGTH_SHORT).show()
                    finish()
                }
                ?.addOnFailureListener {
                    Toast.makeText(this, "Greška kod promjene lozinke: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
