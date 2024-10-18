package com.kyle.quera.db.common

import org.komapper.core.dsl.query.Query
import org.komapper.core.dsl.query.QueryScope
import java.io.Closeable

/**
 * A DAO Facade interface for the Database. This allows to provide several implementations.
 *
 * In this case this is used to provide a Database-based implementation using Komapper.
 */
interface DAOFacade<T> : Closeable {
    /**
     * Initializes all the required data.
     */
//    fun init()

    /**
     * Counts the number of resources.
     */
    suspend fun countResource(): Long

    /**
     * Get the DAO object representation of a resource based from its [id].
     */
    suspend fun getResource(id: Int): T

    /**
     * Get a list of all DAO object representations for this resource. Number of results returned
     * is based on [pagingAndSorting]
     */
    suspend fun getResources(pagingAndSorting: PagingAndSorting): List<T>

    /**
     * Get a list of all DAO object representations for this resource. Number of results returned
     * is based on [pagingAndSorting]
     * [block] - block containing custom QueryDsl to search
     */
    suspend fun getResources(pagingAndSorting: PagingAndSorting, block: QueryScope.() -> Query<List<T>>): List<T>

    /**
     * Creates a resource from an object
     */
    suspend fun createResource(resource: T): T

    /**
     * Deletes a resource by its [id].
     */
    suspend fun deleteResource(id: Int)
}