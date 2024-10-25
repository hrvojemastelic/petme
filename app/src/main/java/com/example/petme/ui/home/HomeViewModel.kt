package com.example.petme.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.petme.R
import com.example.petme.models.ClassifiedAd
import java.util.Date

class HomeViewModel : ViewModel() {

    private val _allAds = MutableLiveData<List<ClassifiedAd>>()
    val allAds: LiveData<List<ClassifiedAd>> = _allAds

    // LiveData for Slideshow images
    private val _slideshowImages = MutableLiveData<List<Int>>()
    val slideshowImages: LiveData<List<Int>> = _slideshowImages

    // LiveData for Recommended Listings (Classified Ads)
    private val _recommendedAds = MutableLiveData<List<ClassifiedAd>>()
    val recommendedAds: LiveData<List<ClassifiedAd>> = _recommendedAds

    init {
        // Initialize slideshow images
        _slideshowImages.value = listOf(
            R.drawable.image1,
            R.drawable.image2,
            R.drawable.image3
        )

        // Initialize recommended ads
        _recommendedAds.value = listOf(
            ClassifiedAd(
                id = "1",
                userId = "user_1",
                title = "Cute Dog",
                description = "A lovely golden retriever.",
                price = 500.0,
                age = 2,
                breed = "Golden Retriever",
                imageUrls = listOf("url_to_image_1", "url_to_image_2"),
                category = "dogs",
                date = "2023-10-01"
            ),
            ClassifiedAd(
                id = "2",
                userId = "user_2",
                title = "Playful Cat",
                description = "A mischievous kitten.",
                price = 300.0,
                age = 1,
                breed = "Siamese",
                imageUrls = listOf("url_to_image_3"),
                category = "cats",
                date = "2023-10-02"
            ),
            ClassifiedAd(
                id = "3",
                userId = "user_3",
                title = "Hamster Cage",
                description = "A spacious cage for hamsters.",
                price = 150.0,
                age = 1,
                breed = "N/A",
                imageUrls = listOf("url_to_image_4"),
                category = "small pets",
                date = "2023-10-02"
            ),
            ClassifiedAd(
                id = "4",
                userId = "user_4",
                title = "Fish Feeders",
                description = "Stylish fish feeders for your aquarium.",
                price = 40.0,
                age = 0,
                breed = "N/A",
                imageUrls = listOf("url_to_image_5"),
                category = "fish",
                date = "2023-10-04"
            ),
            ClassifiedAd(
                id = "5",
                userId = "user_5",
                title = "Turtle Tank",
                description = "A comfortable tank for turtles.",
                price = 100.0,
                age = 3,
                breed = "N/A",
                imageUrls = listOf("url_to_image_6"),
                category = "reptiles",
                date = "2023-10-02"
            )
        )

    }

    // Function to filter ads by category
    fun getAdsByCategory(category: String): LiveData<List<ClassifiedAd>> {
        val filteredAds = MutableLiveData<List<ClassifiedAd>>()

        _allAds.value?.let { ads ->
            filteredAds.value = if (category == "All") {
                ads
            } else {
                ads.filter { it.category == category }
            }
        }

        return filteredAds
    }
}