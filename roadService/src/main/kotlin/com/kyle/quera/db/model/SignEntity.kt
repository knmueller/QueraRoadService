package com.kyle.quera.db.model

import com.kyle.quera.db.common.DAOFacade
import com.kyle.quera.db.common.EntityWrapper
import com.kyle.quera.db.common.PagingAndSorting
import com.kyle.quera.model.Sign
import com.kyle.quera.model.SignMeta
import io.ktor.server.plugins.BadRequestException
import io.r2dbc.spi.R2dbcDataIntegrityViolationException
import org.komapper.annotation.KomapperAutoIncrement
import org.komapper.annotation.KomapperCreatedAt
import org.komapper.annotation.KomapperEntityDef
import org.komapper.annotation.KomapperExperimentalAssociation
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperManyToOne
import org.komapper.annotation.KomapperUpdatedAt
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.Query
import org.komapper.core.dsl.query.QueryScope
import org.komapper.core.dsl.operator.count as dbCount
import org.komapper.core.dsl.query.single

@OptIn(KomapperExperimentalAssociation::class)
@KomapperManyToOne(targetEntity = RoadEntity::class)
@KomapperEntityDef(Sign::class, unit = SignMeta::class)
data class SignEntity(
    @KomapperId
    @KomapperAutoIncrement
    val id: Nothing,
    val roadId: Nothing, // FK
    @KomapperCreatedAt
    val createdAt: Nothing,
    @KomapperUpdatedAt
    val updatedAt: Nothing
)

typealias SignDAOFacade = DAOFacade<Sign>

// This should contain all the Komapper specific implementations
class SignTable : EntityWrapper<_SignEntity, Sign>(SignMeta.sign) {

    override suspend fun count(): Long {
        return database.runQuery {
            QueryDsl.from(entity).select(dbCount())
        }!!
    }

    override suspend fun create(resource: Sign): Sign {
        try {
            return database.runQuery {
                QueryDsl.insert(entity).single(resource)
            }
        } catch (e: R2dbcDataIntegrityViolationException) {
            throw BadRequestException("Road ID doesn't exist")
        }
    }

    override suspend fun delete(id: Int) {
        database.runQuery {
            QueryDsl.delete(entity).where { entity.id eq id }
        }
    }

    override suspend fun find(id: Int): Sign {
        return database.runQuery {
            QueryDsl.from(entity).where { entity.id eq id }.single()
        }
    }

    override suspend fun findAll(pas: PagingAndSorting): List<Sign> {
        return findAll(pas) {
            QueryDsl.from(entity)
                .orderBy(toOrderBy(pas))
                .offset(pas.page * pas.size)
                .limit(pas.size)
        }
    }

    override suspend fun findAll(pas: PagingAndSorting, block: QueryScope.() -> Query<List<Sign>>): List<Sign> {
        return database.runQuery(block)
    }

    override fun close() {
        // Intentionally blank
    }
}