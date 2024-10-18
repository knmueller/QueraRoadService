package com.kyle.quera.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

object IntersectionMeta

@Serializable
data class Intersection(
    var id: Int = 0,
    val name: String,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
)
