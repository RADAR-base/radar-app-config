package org.radarbase.appconfig.persistence.entity

import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.hibernate.annotations.Immutable
import java.time.Instant
import javax.persistence.*

@Entity(name = "Condition")
@Table(name = "condition")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
class ConditionEntity(
    @Column(name = "project_id")
    val projectId: String,
    @Column
    var name: String,
    @Column(name = "last_modified_at")
    val lastModifiedAt: Instant,
    @Column
    @Enumerated(EnumType.STRING)
    var status: EntityStatus,
    @Column
    @Lob
    val expression: String,
    @Column
    val rank: Float = 0.0f,
) {
    @Id
    @GeneratedValue(generator = "condition_id_sequence")
    var id: Long? = null
        protected set
}
