package com.theretros.smartcampus.data.dataclasses

import kotlinx.serialization.Serializable

@Serializable
data class IncidentDetail(
    val title: String,
    val description: String?,
    @Serializable
    //val report_time:Date,
    val location: String,
    val type: String,
    val status: String,
    val images: List<String>
)