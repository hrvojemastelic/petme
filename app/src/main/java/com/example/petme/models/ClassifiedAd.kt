package com.example.petme.models

import java.util.Date
import com.google.firebase.Timestamp


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
    val date:Timestamp,
    val region:String,
    val address:String,
    val phoneNumber:Int,
    val priceType:String

)

{
    // No-argument constructor required for Firebase
    constructor() : this("", "", "", "", 0.0, 0, "", emptyList(), "", Timestamp.now(),"","",0,"")
}