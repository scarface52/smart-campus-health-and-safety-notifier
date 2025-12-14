package com.theretros.smartcampus.data.dataclasses

import kotlinx.serialization.Serializable

@Serializable
data class IncidentSummary(
    val title: String,
    val description: String?,
    @Serializable
    //val report_time:Date,
    val type: String,
    val status: String,
)