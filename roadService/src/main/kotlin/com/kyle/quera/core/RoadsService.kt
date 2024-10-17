package com.kyle.quera.core

import com.kyle.quera.db.common.PagingAndSorting
import com.kyle.quera.db.model.RoadTable
import com.kyle.quera.db.model.intersection
import com.kyle.quera.db.model.road
import com.kyle.quera.model.IntersectionMeta
import com.kyle.quera.route.RoadsRequest
import com.kyle.quera.model.Road
import com.kyle.quera.model.PagedResponse
import com.kyle.quera.model.RoadMeta
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.komapper.core.dsl.QueryDsl
import org.slf4j.LoggerFactory

@Single
class RoadsService : KoinComponent {
    private val logger = LoggerFactory.getLogger(RoadsService::class.java)
    private val db by inject<RoadTable>()

    suspend fun createResource(newRoad: Road): Road {
        return db.createResource(newRoad)
    }

    // TODO update resource
    suspend fun updateResource(road: Road) {
        throw NotImplementedError("update not yet implemented")
    }

    suspend fun getResource(id: Int): Road {
        return db.getResource(id)
    }

    suspend fun getResources(roadsRequest: RoadsRequest): PagedResponse<Road> {
        val pas = PagingAndSorting(
            roadsRequest.sort,
            roadsRequest.page,
            roadsRequest.size
        )

        val roads: List<Road> = if (roadsRequest.intersectionId != null) {
            // find all roads with the same intersection

//            val query = QueryDsl.from(RoadMeta.road)
//                .innerJoin(IntersectionMeta.intersection) {
//                    RoadMeta.road.intersectionId eq IntersectionMeta.intersection.id
//                }
//                .where { RoadMeta.road.intersectionId eq roadsRequest.intersectionId }
//                .orderBy(db.toOrderBy(pas))
//                .offset(pas.page * pas.size)
//                .limit(pas.size)
//                .includeAll()
//
//            val store = db.getResources2(query)
//            for (road in store.roads()) {
////                val intersection = road.intersections(store)
//                logger.info("Road $road")
//            }
//
//            store[RoadMeta.road].toList()

            db.getResources(pas) {
                QueryDsl.from(RoadMeta.road)
                    .innerJoin(IntersectionMeta.intersection) {
                        RoadMeta.road.intersectionId eq IntersectionMeta.intersection.id
                    }
                    .where { RoadMeta.road.intersectionId eq roadsRequest.intersectionId }
                    .orderBy(db.toOrderBy(pas))
                    .offset(pas.page * pas.size)
                    .limit(pas.size)
            }
        } else {
            db.getResources(pas)
        }

        return PagedResponse(
            roads,
            roadsRequest.page,
            total = db.countResource()
        )
    }

    suspend fun deleteResource(id: Int) {
        db.deleteResource(id)
    }
}
