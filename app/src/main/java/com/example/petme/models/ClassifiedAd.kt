package com.example.petme.models

import java.util.Date


data class ClassifiedAd(

    val id: String ,
    val userId:String,
    val title: String,
    val description: String,
    val price: Double,
    val age : Int,
    val breed: String,
    val imageUrls: List<String>,
    val category: String,
    val date:String
)

{
    // No-argument constructor required for Firebase
    constructor() : this("", "", "", "", 0.0, 0, "", emptyList(), "", "")
}