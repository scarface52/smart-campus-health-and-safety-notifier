package com.theretros.smartcampus.data.dataclasses

import kotlinx.serialization.Serializable

@Serializable
data class IncidentClass(
    val class_id: Int,
    val name: Int
)