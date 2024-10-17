package com.kyle.quera.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

object RoadMeta

@Serializable
data class Road(
    val id: Int = 0,
    val surfaceType: String, // todo enum
    val intersectionId: Int, // FK
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
)