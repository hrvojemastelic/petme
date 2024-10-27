package com.example.petme.ui.ads.addad

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.petme.databinding.ActivityAddAdBinding
import com.example.petme.models.ClassifiedAd
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*

class AddAdActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddAdBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private val selectedImages: MutableList<Uri> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddAdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        binding.btnUploadImages.setOnClickListener {
            openImageSelector()
        }

        binding.btnSaveAd.setOnClickListener {
            saveAd()
        }
    }

    private fun openImageSelector() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        startActivityForResult(Intent.createChooser(intent, "Select Images"), IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            // Do not clear the list; instead, keep previously selected images
            if (data?.clipData != null) {
                val count = data.clipData!!.itemCount
                for (i in 0 until count) {
                    val imageUri = data.clipData!!.getItemAt(i).uri
                    if (!selectedImages.contains(imageUri)) {  // Avoid duplicates
                        selectedImages.add(imageUri)
                        addImageToContainer(imageUri)
                    }
                }
            } else if (data?.data != null) {
                val imageUri = data.data!!
                if (!selectedImages.contains(imageUri)) {  // Avoid duplicates
                    selectedImages.add(imageUri)
                    addImageToContainer(imageUri)
                }
            }

            Toast.makeText(this, "${selectedImages.size} images selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addImageToContainer(imageUri: Uri) {
        val imageView = ImageView(this).apply {
            layoutParams = GridLayout.LayoutParams().apply {
                width = 200 // Adjust this size as needed for your layout
                height = 200 // Adjust this size as needed for your layout
                setMargins(8, 8, 8, 8)
            }
            setImageURI(imageUri)
            scaleType = ImageView.ScaleType.CENTER_CROP
            setPadding(4, 4, 4, 4)
            setOnClickListener {
                // Show confirmation dialog before deleting
                AlertDialog.Builder(this@AddAdActivity)
                    .setTitle("Delete Image")
                    .setMessage("Are you sure you want to delete this image?")
                    .setPositiveButton("Yes") { _, _ ->
                        selectedImages.remove(imageUri)
                        binding.selectedImagesContainer.removeView(this)
                        Toast.makeText(this@AddAdActivity, "Image removed", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }.show()
            }
        }
        binding.selectedImagesContainer.addView(imageView)
    }



    private fun saveAd() {
        binding.btnSaveAd.isEnabled = false
        binding.mainContentLayout.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE

        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val priceText = binding.etPrice.text.toString().trim()
        val ageText = binding.etAge.text.toString().trim()
        val category = binding.spinnerCategory.selectedItem.toString()
        val breed = binding.etBreed.text.toString().trim()

        if (title.isEmpty() || description.isEmpty() || priceText.isEmpty() ||
            ageText.isEmpty() || category == "Select Category" || breed.isEmpty() || selectedImages.isEmpty()
        ) {
            binding.btnSaveAd.isEnabled = true
            binding.progressBar.visibility = View.GONE
            binding.mainContentLayout.visibility = View.VISIBLE

            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val price = priceText.toDoubleOrNull()
        val age = ageText.toIntOrNull()

        if (price == null || age == null) {
            binding.btnSaveAd.isEnabled = true
            binding.progressBar.visibility = View.GONE
            binding.mainContentLayout.visibility = View.VISIBLE

            Toast.makeText(this, "Invalid price or age format", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.mainContentLayout.visibility = View.GONE

        uploadImagesAndSaveAd(title, description, price, age, category, breed)
    }

    private fun uploadImagesAndSaveAd(title: String, description: String, price: Double, age: Int, category: String, breed: String) {
        val user = auth.currentUser
        if (user != null && selectedImages.isNotEmpty()) {
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
            val imageRef = storage.reference.child("images/${UUID.randomUUID()}.jpg")

            compressImage(uri)?.let { compressedData ->
                imageRef.putBytes(compressedData)
                    .addOnSuccessListener {
                        imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                            imageUrls.add(downloadUri.toString())
                            uploadImage(index + 1, imageUrls, title, description, price, age, category, breed)
                        }
                    }
                    .addOnFailureListener {
                        binding.btnSaveAd.isEnabled = true
                        binding.progressBar.visibility = View.GONE
                        binding.mainContentLayout.visibility = View.VISIBLE

                        Toast.makeText(this, "Error uploading image", Toast.LENGTH_SHORT).show()
                    }
            } ?: run {
                Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show()
            }
        } else {
            saveAdToFirestore(title, description, price, age, category, breed, imageUrls)
        }
    }

    private fun compressImage(uri: Uri): ByteArray? {
        return try {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri))
            } else {
                MediaStore.Images.Media.getBitmap(contentResolver, uri)
            }

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
            outputStream.toByteArray()
        } catch (e: IOException) {
            null
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
            val adId = UUID.randomUUID().toString()
            val ad = hashMapOf(
                "id" to adId,
                "title" to title,
                "description" to description,
                "price" to price,
                "age" to age,
                "category" to category,
                "breed" to breed,
                "imageUrls" to imageUrls,
                "userId" to user.uid,
                "email" to user.email,
                "dateCreated" to Date()
            )

            firestore.collection("ads")
                .document(adId)
                .set(ad)
                .addOnSuccessListener {
                    binding.btnSaveAd.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    binding.mainContentLayout.visibility = View.VISIBLE

                    Toast.makeText(this, "Ad created successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    binding.btnSaveAd.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    binding.mainContentLayout.visibility = View.VISIBLE

                    Toast.makeText(this, "Error creating ad", Toast.LENGTH_SHORT).show()
                }
        }
    }

    companion object {
        private const val IMAGE_PICK_CODE = 1000
    }
}
