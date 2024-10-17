package com.kyle.quera.db.model

import com.kyle.quera.db.common.DAOFacade
import com.kyle.quera.db.common.EntityWrapper
import com.kyle.quera.db.common.PagingAndSorting
import com.kyle.quera.model.Road
import com.kyle.quera.model.RoadMeta
import io.ktor.server.plugins.BadRequestException
import io.r2dbc.spi.R2dbcDataIntegrityViolationException
import org.komapper.annotation.KomapperAggregateRoot
import org.komapper.annotation.KomapperAutoIncrement
import org.komapper.annotation.KomapperCreatedAt
import org.komapper.annotation.KomapperEntityDef
import org.komapper.annotation.KomapperExperimentalAssociation
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperManyToOne
import org.komapper.annotation.KomapperOneToMany
import org.komapper.annotation.KomapperUpdatedAt
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.Query
import org.komapper.core.dsl.query.QueryScope
import org.komapper.core.dsl.operator.count as dbCount
import org.komapper.core.dsl.query.single

@OptIn(KomapperExperimentalAssociation::class)
@KomapperAggregateRoot("roads")
@KomapperManyToOne(targetEntity = IntersectionEntity::class, navigator = "intersections")
@KomapperOneToMany(targetEntity = SignEntity::class)
@KomapperEntityDef(Road::class, unit = RoadMeta::class)
data class RoadEntity(
    @KomapperId
    @KomapperAutoIncrement
    val id: Nothing,
    val surfaceType: Nothing,
    val intersectionId: Nothing,
    @KomapperCreatedAt
    val createdAt: Nothing,
    @KomapperUpdatedAt
    val updatedAt: Nothing
)

typealias RoadDAOFacade = DAOFacade<Road>

// This should contain all the Komapper specific implementations
class RoadTable : EntityWrapper<_RoadEntity, Road>(RoadMeta.road) {

    override suspend fun count(): Long {
        return database.runQuery {
            QueryDsl.from(entity).select(dbCount())
        }!!
    }

    override suspend fun create(resource: Road): Road {
        try {
            return database.runQuery {
                QueryDsl.insert(entity).single(resource)
            }
        } catch (e: R2dbcDataIntegrityViolationException) {
            throw BadRequestException("Intersection ID doesn't exist")
        }
    }

    override suspend fun delete(id: Int) {
        database.runQuery {
            QueryDsl.delete(entity).where { entity.id eq id }
        }
    }

    override suspend fun find(id: Int): Road {
        return database.runQuery {
            QueryDsl.from(entity).where { entity.id eq id }.single()
        }
    }

    override suspend fun findAll(pas: PagingAndSorting): List<Road> {
        return findAll(pas) {
            QueryDsl.from(entity)
                .orderBy(toOrderBy(pas))
                .offset(pas.page * pas.size)
                .limit(pas.size)
        }
    }

    override suspend fun findAll(pas: PagingAndSorting, block: QueryScope.() -> Query<List<Road>>): List<Road> {
        return database.runQuery(block)
    }

//    override suspend fun findAll2(query: EntityStoreQuery): EntityStore {
//        return database.runQuery(query)
//    }

    override fun close() {
        // Intentionally blank
    }
}