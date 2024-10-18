package com.kyle.quera.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

object RoadMeta

enum class SurfaceType {
    asphalt,
    concrete,
    gravel,
}

@Serializable
data class Road(
    var id: Int = 0,
    val surfaceType: SurfaceType,
    val intersectionId: Int, // FK
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
)