package com.theretros.smartcampus.data.dataclasses

import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
data class IncidentDetail @OptIn(ExperimentalTime::class) constructor(
    val title: String,
    val description: String?,
    val report_time: Instant,
    val location: String,
    val class_id: Int,
    val status: String,
    val images: List<String>
)