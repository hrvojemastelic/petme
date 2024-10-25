package com.example.petme.ui.ads.addad


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.petme.databinding.ActivityAddAdBinding
import com.example.petme.models.ClassifiedAd
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import kotlin.math.log

class AddAdActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddAdBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var selectedImages: List<Uri> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddAdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Upload image button click event
        binding.btnUploadImages.setOnClickListener {
            openImageSelector()
        }

        // Save Ad button
        binding.btnSaveAd.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()
            val priceText = binding.etPrice.text.toString().trim()
            val ageText = binding.etAge.text.toString().trim()
            val category = binding.spinnerCategory.selectedItem.toString()
            val breed = binding.etBreed.text.toString().trim()

            // Check if all fields are filled
            if (title.isEmpty()) {
                Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show()
            } else if (description.isEmpty()) {
                Toast.makeText(this, "Please enter a description", Toast.LENGTH_SHORT).show()
            } else if (priceText.isEmpty()) {
                Toast.makeText(this, "Please enter a price", Toast.LENGTH_SHORT).show()
            } else if (ageText.isEmpty()) {
                Toast.makeText(this, "Please enter age", Toast.LENGTH_SHORT).show()
            } else if (category.isEmpty() || category == "Select Category") {
                Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
            } else if (breed.isEmpty()) {
                Toast.makeText(this, "Please enter breed", Toast.LENGTH_SHORT).show()
            } else if (selectedImages.isEmpty()) {
                Toast.makeText(this, "Please select at least one image", Toast.LENGTH_SHORT).show()
            } else {
                // Convert price and age to respective data types
                val price = priceText.toDoubleOrNull()
                val age = ageText.toIntOrNull()

                if (price == null) {
                    Toast.makeText(this, "Invalid price format", Toast.LENGTH_SHORT).show()
                } else if (age == null) {
                    Toast.makeText(this, "Invalid age format", Toast.LENGTH_SHORT).show()
                } else {
                    // Show the progress bar and proceed to upload images and save ad
                    binding.progressBar.visibility = android.view.View.VISIBLE
                    uploadImagesAndSaveAd(title, description, price, age, category, breed)
                }
            }
        }
    }
        private fun openImageSelector() {
        // Intent for selecting multiple images
        val intent = Intent().apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            action = Intent.ACTION_GET_CONTENT
        }
        startActivityForResult(Intent.createChooser(intent, "Select Images"), IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK) {
            if (data?.clipData != null) {
                // Multiple images selected
                val count = data.clipData?.itemCount ?: 0
                val images = mutableListOf<Uri>()
                for (i in 0 until count) {
                    val imageUri = data.clipData?.getItemAt(i)?.uri
                    if (imageUri != null) {
                        images.add(imageUri)
                    }
                }
                selectedImages = images
                Toast.makeText(this, "$count images selected", Toast.LENGTH_SHORT).show()
            } else if (data?.data != null) {
                // Single image selected
                val imageUri: Uri = data.data!!
                selectedImages = listOf(imageUri)
                Toast.makeText(this, "1 image selected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadImagesAndSaveAd(title: String, description: String, price: Double, age: Int, category: String, breed: String) {
        val user = auth.currentUser
        if (user != null && selectedImages.isNotEmpty()) {
            val storageRef = storage.reference
            val imageUrls = mutableListOf<String>()

            uploadImage(0, imageUrls, title, description, price, age, category, breed)
        }
    }

    private fun uploadImage(
        index: Int,
        imageUrls: MutableList<String>,
        title: String,
        description: String,
        price: Double,
        age: Int,
        category: String,
        breed: String
    ) {
        if (index < selectedImages.size) {
            val uri = selectedImages[index]
            val fileName = UUID.randomUUID().toString()
            val imageRef = storage.reference.child("images/$fileName.jpg")

            val user = auth.currentUser
            if (user != null) {
                Log.d("UploadImage", "Uploading image $index: $uri")
                imageRef.putFile(uri)
                    .addOnSuccessListener {
                        Log.d("UploadImage", "Image $index uploaded successfully")
                        imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                            Log.d("UploadImage", "Image $index download URL: $downloadUri")
                            imageUrls.add(downloadUri.toString())
                            uploadImage(index + 1, imageUrls, title, description, price, age, category, breed)
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("UploadImage", "Error uploading image $index: ${exception.message}")
                        Toast.makeText(this, "Error uploading image at index $index", Toast.LENGTH_SHORT).show()
                        binding.progressBar.visibility = android.view.View.GONE // Hide on failure
                    }
            } else {
                Log.e("UploadImage", "User not authenticated")
                Toast.makeText(this, "Please sign in to upload images", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = android.view.View.GONE // Hide if user is not signed in
            }
        } else {
            Log.d("UploadImage", "All images uploaded successfully")
            saveAdToFirestore(title, description, price, age, category, breed, imageUrls)
        }
    }


    private fun saveAdToFirestore(
        title: String,
        description: String,
        price: Double,
        age: Int,
        category: String,
        breed: String,
        imageUrls: List<String>
    ) {
        val user = auth.currentUser
        if (user != null) {
            // Generate a unique ID for the ad
            val adId = UUID.randomUUID().toString() // Unique ID for the ad

            val ad = hashMapOf(
                "id" to adId, // Include the generated ID
                "title" to title,
                "description" to description,
                "price" to price,
                "age" to age,
                "category" to category,
                "breed" to breed,
                "imageUrls" to imageUrls,
                "userId" to user.uid, // Link ad to current user by user ID
                "email" to user.email,
                "dateCreated" to Date()
            )

            firestore.collection("ads")
                .document(adId) // Use the custom ID to store the ad
                .set(ad) // Use .set() instead of .add()
                .addOnSuccessListener {
                    Toast.makeText(this, "Ad created successfully", Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = android.view.View.GONE // Hide on success
                    finish() // Optionally close the activity after saving
                }
                .addOnFailureListener { exception ->
                    Log.e("FirestoreError", "Error creating ad: ${exception.message}")
                    Toast.makeText(this, "Error creating ad: ${exception.message}", Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = android.view.View.GONE // Hide on failure
                }
        }
    }



    companion object {
        private const val IMAGE_PICK_CODE = 1000
    }
}
