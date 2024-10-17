package com.kyle.quera.model

import kotlinx.serialization.Serializable

@Serializable
data class PagedResponse<T>(
    val elements: List<T>,
    val page: Int,
    val size: Int = elements.size, // keep this order to preserve response order of json keys
    val total: Long,
)