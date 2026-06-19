package com.androidbump.data

data class ContactProfile(
    val fullName: String,
    val phone: String,
    val email: String,
    val company: String = "",
    val website: String = "",
)

data class ProfileResponse(
    val id: String,
    val editToken: String,
    val shareUrl: String,
)
