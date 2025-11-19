package org.radarbase.appconfig.persistence.entity

import jakarta.persistence.Cacheable
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Lob
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.time.Instant

@Entity(name = "Config")
@Table(name = "config")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
class ConfigEntity {
    @Id
    @GeneratedValue(generator = "config_id_sequence")
    @SequenceGenerator(name = "config_id_sequence", allocationSize = 1)
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

    @Column(name = "create_timestamp")
    var createTimestamp: Instant? = null

    @Column(name = "created_by_user")
    var createdByUser: String? = null

    @Column
    var version: Int? = null
}
