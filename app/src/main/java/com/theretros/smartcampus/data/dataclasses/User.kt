package com.theretros.smartcampus.data.dataclasses

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val name: String,
    val last_name: String,
    val email: String,
    val password: String,
    val faculty: String,
    val role: String,
    val jurisdiction: String?
)