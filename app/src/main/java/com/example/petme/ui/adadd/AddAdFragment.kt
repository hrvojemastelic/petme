package com.example.petme.ui.addad

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.icu.lang.UCharacter.toLowerCase
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.petme.R
import com.example.petme.databinding.FragmentAddAdBinding
import com.example.petme.models.ClassifiedAd
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
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

    // Load a scaled down bitmap for display
    // Convert dp to pixels
    private fun Int.dpToPx(): Int =
        (this * resources.displayMetrics.density).toInt()

    // Load scaled thumbnail for display
    private fun loadThumbnail(uri: Uri, maxSize: Int = 200): Bitmap? {
        return try {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(requireContext().contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
            }

            val width = bitmap.width
            val height = bitmap.height
            val scale = minOf(maxSize.toFloat() / width, maxSize.toFloat() / height, 1.0f)

            Bitmap.createScaledBitmap(
                bitmap,
                (width * scale).toInt(),
                (height * scale).toInt(),
                true
            )
        } catch (e: IOException) {
            null
        }
    }

    private fun addImageToContainer(imageUri: Uri) {
        // Load the thumbnail on a background thread
        lifecycleScope.launch(Dispatchers.IO) {
            val thumbnail = loadThumbnail(imageUri, maxSize = 200) // small bitmap for display
            if (thumbnail != null) {
                withContext(Dispatchers.Main) {
                    val imageView = ImageView(requireContext()).apply {
                        scaleType = ImageView.ScaleType.CENTER_CROP
                        setPadding(4.dpToPx(), 4.dpToPx(), 4.dpToPx(), 4.dpToPx())
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

                    // Calculate dynamic column width based on GridLayout width
                    binding.selectedImagesContainer.post {
                        val gridWidth = binding.selectedImagesContainer.width
                        val numColumns = binding.selectedImagesContainer.columnCount
                        val spacing = 8.dpToPx() * (numColumns + 1) // account for margins
                        val columnWidth = (gridWidth - spacing) / numColumns

                        val params = GridLayout.LayoutParams().apply {
                            width = columnWidth
                            height = columnWidth // square image
                            setMargins(4.dpToPx(), 4.dpToPx(), 4.dpToPx(), 4.dpToPx())
                        }
                        imageView.layoutParams = params

                        imageView.setImageBitmap(thumbnail)
                        binding.selectedImagesContainer.addView(imageView)
                    }
                }
            }
        }
    }


    private fun isDarkThemeEnabled(): Boolean {
        val nightModeFlags =
            resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }

    private fun markError(view: View) {
        view.background = ContextCompat.getDrawable(
            requireContext(),
            R.drawable.edittext_bg_error
        )
    }

    private fun clearError(view: View) {
        view.background = ContextCompat.getDrawable(
            requireContext(),
            R.drawable.edittext_bg
        )
    }

    private fun markSpinnerError(spinner: Spinner) {
        spinner.background = ContextCompat.getDrawable(
            requireContext(),
            R.drawable.edittext_bg_error
        )
    }

    private fun clearSpinnerError(spinner: Spinner) {
        spinner.background = ContextCompat.getDrawable(
            requireContext(),
            R.drawable.edittext_bg
        )
    }
    private fun validateRequired(editText: EditText): String? {
        val value = editText.text.toString().trim()
        return if (value.isEmpty()) {
            markError(editText)
            null
        } else {
            clearError(editText)
            value
        }
    }

    private fun validateInt(editText: EditText): Int? {
        val value = editText.text.toString().trim().toIntOrNull()
        return if (value == null) {
            markError(editText)
            null
        } else {
            clearError(editText)
            value
        }
    }

    private fun validateDouble(editText: EditText): Double? {
        val value = editText.text.toString().trim().toDoubleOrNull()
        return if (value == null) {
            markError(editText)
            null
        } else {
            clearError(editText)
            value
        }
    }

    private fun validateSpinner(spinner: Spinner, invalidValue: String): String? {
        val value = spinner.selectedItem.toString()
        return if (value == invalidValue) {
            markSpinnerError(spinner)
            null
        } else {
            clearSpinnerError(spinner)
            value
        }
    }

    private fun restoreUi() {
        binding.btnSaveAd.isEnabled = true
        binding.progressBar.visibility = View.GONE
        binding.mainContentLayout.visibility = View.VISIBLE
    }



    private fun saveAd() {
        binding.btnSaveAd.isEnabled = false
        binding.mainContentLayout.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE

        if (auth.currentUser == null) {
            restoreUi()
            Toast.makeText(requireContext(), "Sva polja su obavezna", Toast.LENGTH_SHORT).show()
            return
        }

        var hasError = false

        val title = validateRequired(binding.etTitle) ?: run { hasError = true; "" }
        binding.etTitle.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrBlank()) {
                    clearError(binding.etTitle)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        val description = validateRequired(binding.etDescription) ?: run { hasError = true; "" }
        binding.etDescription.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrBlank()) {
                    clearError(binding.etDescription)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        val breed = validateRequired(binding.etBreed) ?: run { hasError = true; "" }
        binding.etBreed.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrBlank()) {
                    clearError(binding.etBreed)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        val address = validateRequired(binding.address) ?: run { hasError = true; "" }
        binding.address.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrBlank()) {
                    clearError(binding.address)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        val price = validateDouble(binding.etPrice) ?: run { hasError = true; 0.0 }
        binding.etPrice.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrBlank()) {
                    clearError(binding.etPrice)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        val age = validateInt(binding.etAge) ?: run { hasError = true; 0 }
        binding.etAge.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrBlank()) {
                    clearError(binding.etAge)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        val phoneNumber = validateInt(binding.phoneNumber) ?: run { hasError = true; 0 }
        binding.phoneNumber.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrBlank()) {
                    clearError(binding.phoneNumber)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        val category = validateSpinner(binding.spinnerCategory, "Odaberi kategoriju")
            ?: run { hasError = true; "" }

        binding.spinnerCategory.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    clearSpinnerError(binding.spinnerCategory)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

        val region = validateSpinner(binding.spinner, "Odaberi županiju")
            ?: run { hasError = true; "" }
        binding.spinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    clearSpinnerError(binding.spinner)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        val typeOfAd = validateSpinner(binding.typeOfAd, "Odaberi tip oglasa")
            ?: run { hasError = true; "" }
        binding.typeOfAd.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    clearSpinnerError(binding.typeOfAd)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        val bloodType = validateSpinner(binding.bloodType, "Odaberi tip")
            ?: run { hasError = true; "" }
        binding.bloodType.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    clearSpinnerError(binding.bloodType)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        val chip = validateSpinner(binding.chip, "Je li čipiran?")
            ?: run { hasError = true; "" }
        binding.chip.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    clearSpinnerError(binding.chip)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        val vaccine = validateSpinner(binding.vacinated, "Je li cijepljen?")
            ?: run { hasError = true; "" }
        binding.vacinated.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    clearSpinnerError(binding.vacinated)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        if (selectedImages.isEmpty()) {
            hasError = true
            Toast.makeText(requireContext(), "Molim dodajte slike", Toast.LENGTH_SHORT).show()
        }

        if (hasError) {
            restoreUi()
            Toast.makeText(requireContext(), "Sva polja su obavezna", Toast.LENGTH_SHORT).show()
            return
        }

        // ✅ All fields valid → continue with save logic here

    uploadImagesAndSaveAd(
            title,
            description,
            price,
            age,
            category,
            breed,
            region,
            address,
            phoneNumber,
            typeOfAd,
            bloodType,
            chip,
            vaccine
        )
    }



    private fun uploadImagesAndSaveAd(title: String, description: String, price: Double, age: Int, category: String, breed: String,region:String,address:String,phoneNumber: Int,typeOfAd : String,bloodType:String,chip:String,vaccine:String) {
        val user = auth.currentUser
        if (user != null && selectedImages.isNotEmpty()) {
            val imageUrls = mutableListOf<String>()
            uploadImageAndAdd(0, imageUrls, title, description, price, age, category, breed,region,address,phoneNumber,typeOfAd,bloodType,chip,vaccine)
        }
    }

    private fun uploadImageAndAdd(
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
        bloodType:String,
        chip:String,
        vaccine: String
    ) {
        if (index >= selectedImages.size) {
            saveAdToFirestore(title, description, price, age, category, breed, imageUrls, region, address, phoneNumber, typeOfAd, bloodType, chip, vaccine)
            return
        }

        val uri = selectedImages[index]
        lifecycleScope.launch(Dispatchers.IO) {
            val compressedData = compressImage(uri)
            if (compressedData == null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error processing image", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            val imageRef = storage.reference.child("images/${UUID.randomUUID()}.jpg")
            val uploadTask = imageRef.putBytes(compressedData).await() // Using kotlinx-coroutines-play-services

            val downloadUrl = imageRef.downloadUrl.await()
            imageUrls.add(downloadUrl.toString())

            withContext(Dispatchers.Main) {
                // Recursive call on main thread to keep UI safe
                uploadImageAndAdd(index + 1, imageUrls, title, description, price, age, category, breed, region, address, phoneNumber, typeOfAd, bloodType, chip, vaccine)
            }
        }
    }

    private fun compressImage(uri: Uri): ByteArray? {
        return try {
            val maxDimension = 1024 // max width or height
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(requireContext().contentResolver, uri)
                var bmp = ImageDecoder.decodeBitmap(source)
                bmp = scaleBitmap(bmp, maxDimension)
                bmp
            } else {
                val bmp = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
                scaleBitmap(bmp, maxDimension)
            }

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
            outputStream.toByteArray()
        } catch (e: IOException) {
            null
        }
    }

    private fun scaleBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val scale = if (width > height) {
            maxDimension.toFloat() / width
        } else {
            maxDimension.toFloat() / height
        }

        return Bitmap.createScaledBitmap(
            bitmap,
            (width * scale).toInt(),
            (height * scale).toInt(),
            true
        )
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
        bloodType:String,
        chip: String,vaccine: String
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
                "bloodType" to bloodType,
                "chip" to chip,
                "vaccine" to vaccine
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
