package org.radarbase.appconfig.persistence.entity

import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import jakarta.persistence.*

@Entity(name = "Config")
@Table(name = "config")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
class ConfigEntity {
    @Id
    @GeneratedValue(generator = "config_id_sequence")
    var id: Long? = null
        private set

    @Column
    lateinit var scope: String

    @Column(name = "client_id")
    lateinit var clientId: String

    @Column
    lateinit var name: String

    @Column
    @Lob
    var value: String? = null
}
