package com.example.foodrecipe

data class Character(var gender: String = "Male",
                     var skinTone: Int = R.drawable.gemma, // Add default drawable
                     var hairStyle: Int = R.drawable.gemma,  // Add default drawable
                     var outfit: Int = R.drawable.gemma      )
