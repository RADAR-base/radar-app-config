package org.radarbase.appconfig.persistence.entity

import jakarta.persistence.*
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.hibernate.annotations.Immutable

@Entity(name = "Config")
@Table(name = "config")
@Cacheable
@Immutable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
class ConfigEntity(
    @Column
    val name: String,

    @Column
    val rank: Float = 0.0f,

    @ManyToOne(cascade = [CascadeType.REFRESH], fetch = FetchType.LAZY)
    val state: ConfigStateEntity,

    @Column
    @Lob
    val value: String?
) {
    @Id
    @GeneratedValue(generator = "config_id_sequence")
    @SequenceGenerator(name = "config_id_sequence", allocationSize = 1)
    var id: Long? = null
        private set
}
