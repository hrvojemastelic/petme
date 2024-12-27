package com.example.petme.ui.addad

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.petme.R
import com.example.petme.databinding.FragmentAddAdBinding
import com.example.petme.models.ClassifiedAd
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*

class AddAdFragment : Fragment() {

    private lateinit var binding: FragmentAddAdBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private val selectedImages: MutableList<Uri> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddAdBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        binding.btnUploadImages.setOnClickListener {
            openImageSelector()
        }

        binding.btnSaveAd.setOnClickListener {
            saveAd()
        }

        return binding.root
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

            Toast.makeText(requireContext(), "${selectedImages.size} images selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addImageToContainer(imageUri: Uri) {
        val imageView = ImageView(requireContext()).apply {
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
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Image")
                    .setMessage("Are you sure you want to delete this image?")
                    .setPositiveButton("Yes") { _, _ ->
                        selectedImages.remove(imageUri)
                        binding.selectedImagesContainer.removeView(this)
                        Toast.makeText(requireContext(), "Image removed", Toast.LENGTH_SHORT).show()
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
        val region = binding.spinner.selectedItem.toString()
        val address = binding.address.text.toString().trim()
        val phoneNumberText = binding.phoneNumber.text.toString().trim()
        val typeOfAd = binding.typeOfAd.selectedItem.toString()
        val bloodType = binding.bloodType.selectedItem.toString()
        if (title.trim().isEmpty() || description.trim().isEmpty() || priceText.isEmpty() ||
            ageText.isEmpty() || category == "Select Category" || category.trim().isEmpty() || breed.isEmpty() || selectedImages.isEmpty()
            || region == "Select Region" || region.trim().isEmpty() || address.trim().isEmpty() || phoneNumberText.trim().isEmpty() || typeOfAd =="Select type of ad" ||
            typeOfAd.trim().isEmpty() || bloodType == "Select blood" || bloodType.trim().isEmpty()
        ) {
            binding.btnSaveAd.isEnabled = true
            binding.progressBar.visibility = View.GONE
            binding.mainContentLayout.visibility = View.VISIBLE

            Toast.makeText(requireContext(), "Please fill in all fields and add pictures", Toast.LENGTH_SHORT).show()
            return
        }

        val price = priceText.toDoubleOrNull()
        val age = ageText.toIntOrNull()
        val phoneNumber = phoneNumberText.toIntOrNull()

        if (price == null || age == null || phoneNumber == null) {
            binding.btnSaveAd.isEnabled = true
            binding.progressBar.visibility = View.GONE
            binding.mainContentLayout.visibility = View.VISIBLE

            Toast.makeText(requireContext(), "Invalid price or age or phone number format", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.mainContentLayout.visibility = View.GONE

        uploadImagesAndSaveAd(title, description, price, age, category, breed,region,address,phoneNumber,typeOfAd,bloodType)
    }

    private fun uploadImagesAndSaveAd(title: String, description: String, price: Double, age: Int, category: String, breed: String,region:String,address:String,phoneNumber: Int,typeOfAd : String,bloodType:String) {
        val user = auth.currentUser
        if (user != null && selectedImages.isNotEmpty()) {
            val imageUrls = mutableListOf<String>()
            uploadImage(0, imageUrls, title, description, price, age, category, breed,region,address,phoneNumber,typeOfAd,bloodType)
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
        breed: String,
        region:String,
        address:String,
        phoneNumber: Int,
        typeOfAd: String,
        bloodType:String
    ) {
        if (index < selectedImages.size) {
            val uri = selectedImages[index]
            val imageRef = storage.reference.child("images/${UUID.randomUUID()}.jpg")

            compressImage(uri)?.let { compressedData ->
                imageRef.putBytes(compressedData)
                    .addOnSuccessListener {
                        imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                            imageUrls.add(downloadUri.toString())
                            uploadImage(index + 1, imageUrls, title, description, price, age, category, breed,region,address,phoneNumber,typeOfAd,bloodType)
                        }
                    }
                    .addOnFailureListener {
                        binding.btnSaveAd.isEnabled = true
                        binding.progressBar.visibility = View.GONE
                        binding.mainContentLayout.visibility = View.VISIBLE

                        Toast.makeText(requireContext(), "Error uploading image", Toast.LENGTH_SHORT).show()
                    }
            } ?: run {
                Toast.makeText(requireContext(), "Error processing image", Toast.LENGTH_SHORT).show()
            }
        } else {
            saveAdToFirestore(title, description, price, age, category, breed, imageUrls,region,address,phoneNumber,typeOfAd,bloodType)
        }
    }

    private fun compressImage(uri: Uri): ByteArray? {
        return try {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(requireContext().contentResolver, uri))
            } else {
                MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
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
        imageUrls: List<String>,
        region: String,
        address: String,
        phoneNumber: Int,
        typeOfAd: String,
        bloodType:String
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
                "dateCreated" to Date(),
                "region" to region,
                "address" to address,
                "phoneNumber" to phoneNumber,
                "typeOfAd" to typeOfAd,
                "bloodType" to bloodType
            )

            firestore.collection("ads")
                .document(adId)
                .set(ad)
                .addOnSuccessListener {
                    binding.btnSaveAd.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    binding.mainContentLayout.visibility = View.VISIBLE

                    Toast.makeText(requireContext(), "Ad created successfully", Toast.LENGTH_SHORT).show()
                    activity?.onBackPressed() // Go back to the previous fragment or activity
                }
                .addOnFailureListener {
                    binding.btnSaveAd.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    binding.mainContentLayout.visibility = View.VISIBLE

                    Toast.makeText(requireContext(), "Error creating ad", Toast.LENGTH_SHORT).show()
                }
        }
    }

    companion object {
        private const val IMAGE_PICK_CODE = 1000
    }
}
