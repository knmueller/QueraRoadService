package com.kyle.quera.core

import com.kyle.quera.db.common.PagingAndSorting
import com.kyle.quera.db.model.SignTable
import com.kyle.quera.db.model.road
import com.kyle.quera.db.model.sign
import com.kyle.quera.route.SignsRequest
import com.kyle.quera.model.Sign
import com.kyle.quera.model.PagedResponse
import com.kyle.quera.model.RoadMeta
import com.kyle.quera.model.SignMeta
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.komapper.core.dsl.QueryDsl

@Single
class SignsService : KoinComponent {
    private val db by inject<SignTable>()

    suspend fun createResource(newSign: Sign): Sign {
        return db.createResource(newSign)
    }

    // TODO update resource
    suspend fun updateResource(sign: Sign) {
        throw NotImplementedError("update not yet implemented")
    }

    suspend fun getResource(id: Int): Sign {
        return db.getResource(id)
    }

    suspend fun getResources(signsRequest: SignsRequest): PagedResponse<Sign> {
        val pas = PagingAndSorting(
            signsRequest.sort,
            signsRequest.page,
            signsRequest.size
        )

        val signs: List<Sign> = if (signsRequest.roadId != null) {
            // find all signs on a specific road
            db.getResources(pas) {
                QueryDsl.from(SignMeta.sign)
                    .innerJoin(RoadMeta.road) {
                        SignMeta.sign.roadId eq RoadMeta.road.id
                    }
                    .where { SignMeta.sign.roadId eq signsRequest.roadId }
                    .orderBy(db.toOrderBy(pas))
                    .offset(pas.page * pas.size)
                    .limit(pas.size)
            }
        } else {
            db.getResources(pas)
        }

        return PagedResponse(
            signs,
            signsRequest.page,
            total = db.countResource()
        )
    }

    suspend fun deleteResource(id: Int) {
        db.deleteResource(id)
    }
}
