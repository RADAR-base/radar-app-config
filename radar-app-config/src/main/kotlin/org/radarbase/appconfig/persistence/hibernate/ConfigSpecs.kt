package org.radarbase.appconfig.persistence.hibernate

import org.radarbase.appconfig.persistence.entity.ConfigEntity
import org.radarbase.lang.expression.QualifiedId
import org.radarbase.lang.expression.Scope

object ConfigSpecs {
    fun whereClientId(clientId: String) = HibernateSpec<ConfigEntity> { root, _, cb ->
        cb.equal(root.get<String>("clientId"), clientId)
    }

    fun whereScopes(scopes: Collection<Scope>?) = HibernateSpec<ConfigEntity> { root, _, _ ->
        if (scopes.isNullOrEmpty()) {
            null
        } else {
            root.get<String>("scope").`in`(scopes.map { it.asString() })
        }
    }

    fun whereId(id: QualifiedId?) = HibernateSpec<ConfigEntity> { root, _, cb ->
        if (id == null) null else cb.equal(root.get<String>("name"), id.asString())
    }

    fun whereIdHasPrefix(prefix: QualifiedId?) = HibernateSpec<ConfigEntity> { root, _, cb ->
        if (prefix == null) null else cb.like(root.get<String>("name"), prefix.asString() + "%")
    }

    fun whereVersion(version: Int?) = HibernateSpec<ConfigEntity> { root, _, cb ->
        if (version == null) null else cb.equal(root.get<Int>("version"), version)
    }

    fun ofLatestVersion() = HibernateSpec<ConfigEntity> { root, query, cb ->
        val subquery = query.subquery(Int::class.java)
        val subRoot = subquery.from(ConfigEntity::class.java)

        subquery.select(cb.max(subRoot.get("version")))
        subquery.where(
            cb.equal(subRoot.get<String>("scope"), root.get<String>("scope")),
            cb.equal(subRoot.get<String>("clientId"), root.get<String>("clientId")),
            cb.equal(subRoot.get<String>("name"), root.get<String>("name")),
        )

        cb.equal(root.get<Int>("version"), subquery)
    }
}
