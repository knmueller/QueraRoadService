package com.kyle.quera.model

import java.time.LocalDateTime
import kotlinx.serialization.Serializable

object SignMeta

@Serializable
data class Sign(
    var id: Int = 0,
    val roadId: Int, // FK
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime? = null,
    @Serializable(with = LocalDateTimeSerializer::class)
    val updatedAt: LocalDateTime? = null,
)