package com.kyle.quera.core

import com.kyle.quera.db.common.PagingAndSorting
import com.kyle.quera.db.model.IntersectionTable
import com.kyle.quera.route.IntersectionsRequest
import com.kyle.quera.model.Intersection
import com.kyle.quera.model.PagedResponse
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Single
class IntersectionsService : KoinComponent {
    private val db by inject<IntersectionTable>()

    suspend fun createResource(newIntersection: Intersection): Intersection {
        return db.createResource(newIntersection)
    }

    // TODO update resource
    suspend fun updateResource(intersection: Intersection) {
        throw NotImplementedError("update not yet implemented")
    }

    suspend fun getResource(id: Int): Intersection {
        return db.getResource(id)
    }

    suspend fun getResources(intersectionsRequest: IntersectionsRequest): PagedResponse<Intersection> {
        return PagedResponse(
            db.getResources(
                PagingAndSorting(
                    intersectionsRequest.sort,
                    intersectionsRequest.page,
                    intersectionsRequest.size
                )
            ),
            intersectionsRequest.page,
            total = db.countResource()
        )
    }

    suspend fun deleteResource(id: Int) {
        db.deleteResource(id)
    }
}
