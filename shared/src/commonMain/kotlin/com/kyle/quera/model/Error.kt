package com.kyle.quera.model

import BadRequestCode
import BadRequestMessage
import NotFoundCode
import NotFoundMessage
import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val message: String,
    val code: Int,
    var details: String = ""
)

// TODO re-use built-in ktor messages and codes.
val NotFoundErrorRsp = ErrorResponse(NotFoundMessage, NotFoundCode)
val BadRequestErrorRsp = ErrorResponse(BadRequestMessage, BadRequestCode)
