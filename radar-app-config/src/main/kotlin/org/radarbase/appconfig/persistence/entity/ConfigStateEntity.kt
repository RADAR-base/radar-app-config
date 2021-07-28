package org.radarbase.appconfig.persistence.entity

import java.time.Instant
import javax.persistence.*
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.hibernate.annotations.Immutable

@Entity(name = "ConfigState")
@Table(name = "config_state")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
class ConfigStateEntity(
    @Column
    val scope: String,
    @Column(name = "client_id")
    val clientId: String,
    @Column(name = "last_modified_at")
    val lastModifiedAt: Instant,
    @Column(name = "deactivated_at")
    var deactivatedAt: Instant? = null,
    @Column
    @Enumerated(EnumType.STRING)
    var status: EntityStatus,
    @Immutable
    @OneToMany(
        targetEntity = ConfigEntity::class,
        cascade = [CascadeType.ALL],
        fetch = FetchType.EAGER,
        mappedBy = "state",
    )
    @MapKey(name = "name")
    @OrderBy(value = "rank, name")
    var values: Map<String, ConfigEntity>,
) {
    @Id
    @GeneratedValue(generator = "config_state_id_sequence")
    var id: Long? = null
        protected set
}
