package com.kyle.quera.db.model

import com.kyle.quera.db.common.DAOFacade
import com.kyle.quera.db.common.EntityWrapper
import com.kyle.quera.db.common.PagingAndSorting
import com.kyle.quera.model.Intersection
import com.kyle.quera.model.IntersectionMeta
import org.komapper.annotation.KomapperAutoIncrement
import org.komapper.annotation.KomapperCreatedAt
import org.komapper.annotation.KomapperEntityDef
import org.komapper.annotation.KomapperExperimentalAssociation
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperOneToMany
import org.komapper.annotation.KomapperUpdatedAt
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.Query
import org.komapper.core.dsl.query.QueryScope
import org.komapper.core.dsl.operator.count as dbCount
import org.komapper.core.dsl.query.single

@OptIn(KomapperExperimentalAssociation::class)
@KomapperOneToMany(targetEntity = RoadEntity::class)
@KomapperEntityDef(Intersection::class, unit = IntersectionMeta::class)
data class IntersectionEntity(
    @KomapperId
    @KomapperAutoIncrement
    val id: Nothing,
    val name: Nothing,
    @KomapperCreatedAt
    val createdAt: Nothing,
    @KomapperUpdatedAt
    val updatedAt: Nothing
)

typealias IntersectionDAOFacade = DAOFacade<Intersection>

// This should contain all the Komapper specific implementations
class IntersectionTable : EntityWrapper<_IntersectionEntity, Intersection>(IntersectionMeta.intersection) {

    override suspend fun count(): Long {
        return database.runQuery {
            QueryDsl.from(entity).select(dbCount())
        }!!
    }

    override suspend fun create(resource: Intersection): Intersection {
        return database.runQuery {
            QueryDsl.insert(entity).single(resource)
        }
    }

    override suspend fun delete(id: Int) {
        database.runQuery {
            QueryDsl.delete(entity).where { entity.id eq id }
        }
    }

    override suspend fun find(id: Int): Intersection {
        return database.runQuery {
            QueryDsl.from(entity).where { entity.id eq id }.single()
        }
    }

    override suspend fun findAll(pas: PagingAndSorting): List<Intersection> {
        return findAll(pas) {
            QueryDsl.from(entity)
                .orderBy(toOrderBy(pas))
                .offset(pas.page * pas.size)
                .limit(pas.size)
        }
    }

    override suspend fun findAll(pas: PagingAndSorting, block: QueryScope.() -> Query<List<Intersection>>): List<Intersection> {
        return database.runQuery(block)
    }

    override fun close() {
        // Intentionally blank
    }
}