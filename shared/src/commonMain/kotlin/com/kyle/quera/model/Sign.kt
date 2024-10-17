package com.kyle.quera.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

object SignMeta

@Serializable
data class Sign(
    val id: Int = 0,
    val roadId: Int, // FK
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
)