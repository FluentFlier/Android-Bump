package com.androidbump.data

data class ContactProfile(
    val fullName: String,
    val phone: String,
    val email: String = "",
    val company: String = "",
    val website: String = "",
)
