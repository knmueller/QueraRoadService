package com.kyle.quera.model

import java.time.LocalDateTime
import kotlinx.serialization.Serializable

object IntersectionMeta

@Serializable
data class Intersection(
    var id: Int = 0,
    val name: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime? = null,
    @Serializable(with = LocalDateTimeSerializer::class)
    val updatedAt: LocalDateTime? = null,
)
