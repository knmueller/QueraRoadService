package com.kyle.quera

import com.kyle.quera.model.Intersection
import com.kyle.quera.model.Road
import com.kyle.quera.model.Sign
import com.kyle.quera.model.SurfaceType
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import kotlin.random.Random
import kotlin.random.nextInt

fun Intersection.Companion.generate(): Sequence<Intersection> = sequence {
    while (true) {
        val created = LocalDateTime(
            Random.nextInt(2010, 2024),
            Random.nextInt(1, 13),
            Random.nextInt(1, 29),
            Random.nextInt(0, 24),
            Random.nextInt(0, 60),
            Random.nextInt(0, 60)
        ).toJavaLocalDateTime()

        yield(
            Intersection(
                id = Random.nextInt(0, 100),
                name = String.generate(10),
                createdAt = created,
                updatedAt = LocalDateTime(
                    created.year,
                    created.month + 1,
                    created.dayOfMonth,
                    0,
                    0,
                    0
                ).toJavaLocalDateTime()
            )
        )
    }
}

fun Road.Companion.generate(intersectionId: Int): Sequence<Road> = sequence {
    while (true) {
        val created = LocalDateTime(
            Random.nextInt(2010, 2024),
            Random.nextInt(1, 13),
            Random.nextInt(1, 29),
            Random.nextInt(0, 24),
            Random.nextInt(0, 60),
            Random.nextInt(0, 60)
        ).toJavaLocalDateTime()
        yield(
            Road(
                id = Random.nextInt(0, 100),
                surfaceType = SurfaceType.entries[Random.nextInt(0..2)],
                intersectionId = intersectionId,
                createdAt = created,
                updatedAt = LocalDateTime(
                    created.year,
                    created.month + 1,
                    created.dayOfMonth,
                    0,
                    0,
                    0
                ).toJavaLocalDateTime()
            )
        )
    }
}

fun Sign.Companion.generate(roadId: Int): Sequence<Sign> = sequence {
    while (true) {
        val created = LocalDateTime(
            Random.nextInt(2010, 2024),
            Random.nextInt(1, 13),
            Random.nextInt(1, 29),
            Random.nextInt(0, 24),
            Random.nextInt(0, 60),
            Random.nextInt(0, 60)
        ).toJavaLocalDateTime()
        yield(
            Sign(
                id = Random.nextInt(0, 100),
                roadId = roadId,
                createdAt = created,
                updatedAt = LocalDateTime(
                    created.year,
                    created.month + 1,
                    created.dayOfMonth,
                    0,
                    0,
                    0
                ).toJavaLocalDateTime()
            )
        )
    }
}

private fun String.Companion.generate(length: Int): String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return String(CharArray(length) { allowedChars.random() })
}
