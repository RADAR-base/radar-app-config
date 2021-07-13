package org.radarbase.appconfig.persistence.entity

import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.hibernate.annotations.Immutable
import javax.persistence.*

@Entity(name = "Config")
@Table(name = "config")
@Cacheable
@Immutable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
class ConfigEntity {
    @Id
    @GeneratedValue(generator = "config_id_sequence")
    var id: Long? = null
        private set

    @Column
    lateinit var name: String

    @Column
    @Lob
    var value: String? = null

    @Column
    var rank: Int = 0

    @ManyToOne(cascade = [CascadeType.REFRESH], fetch = FetchType.LAZY)
    var state: ConfigStateEntity? = null
}
