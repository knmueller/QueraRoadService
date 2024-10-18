package com.kyle.quera.model

import java.time.LocalDateTime
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
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime? = null,
    @Serializable(with = LocalDateTimeSerializer::class)
    val updatedAt: LocalDateTime? = null,
)