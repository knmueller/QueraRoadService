package com.kyle.quera

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.serializer

class Utils {
    companion object {
        val json = Json {
            prettyPrint = true
            encodeDefaults = true
        }

        @OptIn(InternalSerializationApi::class)
        inline fun <reified I, reified O: Any> Json.convert(from: I): O {
            return json.decodeFromJsonElement(O::class.serializer(), json.encodeToJsonElement(from))
        }
    }
}