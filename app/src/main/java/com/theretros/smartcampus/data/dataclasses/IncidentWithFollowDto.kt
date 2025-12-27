package com.theretros.smartcampus.data.dataclasses

import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
data class IncidentWithFollowDto @OptIn(ExperimentalTime::class) constructor(
    val incident_id: Int,
    val title: String,
    val description: String?,
    val report_time: Instant,
    val class_id: Int,
    val status: String,
    val followed_incidents: List<FollowedIncidentDto> = emptyList()
)

@Serializable
data class FollowedIncidentDto(
    val user_id: Int
)

@OptIn(ExperimentalTime::class)
fun IncidentWithFollowDto.toDomain(): IncidentWithFollow =
    IncidentWithFollow(
        incident_id = incident_id,
        title = title,
        description = description,
        report_time = report_time,
        class_id = class_id,
        status = status,
        is_followed = followed_incidents.isNotEmpty()
    )