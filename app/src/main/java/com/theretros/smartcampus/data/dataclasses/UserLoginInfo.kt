package com.theretros.smartcampus.data.dataclasses

import kotlinx.serialization.Serializable

@Serializable
data class UserLoginInfo(
    val email: String,
    val password: String
)