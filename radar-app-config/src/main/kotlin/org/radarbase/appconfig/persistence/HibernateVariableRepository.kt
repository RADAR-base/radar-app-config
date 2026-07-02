package org.radarbase.appconfig.persistence

import jakarta.inject.Provider
import jakarta.persistence.EntityManager
import jakarta.persistence.TypedQuery
import org.radarbase.appconfig.api.SingleVariable
import org.radarbase.appconfig.persistence.entity.ConfigEntity
import org.radarbase.appconfig.persistence.hibernate.ConfigSpecs.ofLatestVersion
import org.radarbase.appconfig.persistence.hibernate.ConfigSpecs.whereClientId
import org.radarbase.appconfig.persistence.hibernate.ConfigSpecs.whereId
import org.radarbase.appconfig.persistence.hibernate.ConfigSpecs.whereIdHasPrefix
import org.radarbase.appconfig.persistence.hibernate.ConfigSpecs.whereScopes
import org.radarbase.appconfig.persistence.hibernate.ConfigSpecs.whereVersion
import org.radarbase.jersey.hibernate.HibernateRepository
import org.radarbase.jersey.service.AsyncCoroutineService
import org.radarbase.lang.expression.NullLiteral
import org.radarbase.lang.expression.QualifiedId
import org.radarbase.lang.expression.ResolvedVariable
import org.radarbase.lang.expression.Scope
import org.radarbase.lang.expression.SimpleScope
import org.radarbase.lang.expression.toVariable
import kotlin.streams.asSequence

/*
* Runs variables from the database that are not resolved in a scope
* Acts as a normal database querying interface.
* */
class HibernateVariableRepository(
    em: Provider<EntityManager>,
    private val clientId: String,
    asyncCoroutineService: AsyncCoroutineService,
) : VariableRepository, HibernateRepository(em, asyncCoroutineService) {

    override suspend fun query(
        scopes: Collection<Scope>?,
        id: QualifiedId?,
        prefix: QualifiedId?,
        version: Int?,
    ): Sequence<ConfigEntity> = transact {
        freeConfigQuery(scopes, id, prefix, version)
            .resultStream
            .asSequence()
    }

    private fun EntityManager.freeConfigQuery(
        scopes: Collection<Scope>? = null,
        id: QualifiedId? = null,
        prefix: QualifiedId? = null,
        version: Int? = null,
    ): TypedQuery<ConfigEntity> {
        val cb = criteriaBuilder
        val query = cb.createQuery(ConfigEntity::class.java)
        val root = query.from(ConfigEntity::class.java)

        // Add predicates to the query.
        buildList {
            add(whereClientId(clientId))
            add(whereScopes(scopes))
            add(whereId(id))
            add(whereIdHasPrefix(prefix))
            if (version != null) {
                add(whereVersion(version))
            } else {
                add(ofLatestVersion())
            }
        }
            .mapNotNull { it.toPredicate(root, query, cb) }
            .toTypedArray()
            .let {
                query.where(*it)
            }

        return createQuery(query)
    }

    companion object {
        private fun ConfigEntity.toResolvedVariable() = ResolvedVariable(
            SimpleScope(scope),
            QualifiedId(name),
            value?.toVariable() ?: NullLiteral(),
            createTimestamp,
            createdByUser,
            version,
        )
    }
}
