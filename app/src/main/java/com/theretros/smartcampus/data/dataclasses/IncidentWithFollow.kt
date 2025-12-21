package com.theretros.smartcampus.data.dataclasses

import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
data class IncidentWithFollow @OptIn(ExperimentalTime::class) constructor(
    val incident_id: Int,
    val title: String,
    val description: String?,
    val report_time: Instant,
    val class_id: Int,
    val status: String,
    val is_followed: Boolean
)