package org.radarbase.appconfig.persistence.entity

import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.hibernate.annotations.Immutable
import java.time.Instant
import javax.persistence.*

@Entity(name = "ConfigState")
@Table(name = "config_state")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
class ConfigStateEntity {
    @Id
    @GeneratedValue(generator = "config_state_id_sequence")
    var id: Long? = null
        private set

    @Column
    lateinit var scope: String

    @Column(name = "client_id")
    lateinit var clientId: String

    @Column(name = "last_modified_at")
    lateinit var lastModifiedAt: Instant

    @Column
    @Enumerated(EnumType.STRING)
    lateinit var status: Status

    @Immutable
    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER, mappedBy = "state")
    @MapKey(name = "name")
    @OrderBy(value = "rank, name")
    lateinit var values: Map<String, ConfigEntity>

    enum class Status {
        ACTIVE, INACTIVE
    }
}
