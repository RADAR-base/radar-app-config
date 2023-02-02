package org.radarbase.appconfig.persistence.entity

import jakarta.persistence.*
import java.time.Instant
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy

@Entity(name = "Condition")
@Table(name = "condition")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
class ConditionEntity(
    @Column(name = "project_id")
    val projectId: String,
    @Column
    val name: String,
    @Column
    var title: String?,
    @Column(name = "created_at")
    val createdAt: Instant? = null,
    @Column(name = "last_modified_at")
    var lastModifiedAt: Instant,
    @Column(name = "deactivated_at")
    var deactivatedAt: Instant? = null,
    @Column
    @Enumerated(EnumType.STRING)
    var status: EntityStatus,
    @Column
    @Lob
    var expression: String?,
    @Column
    var rank: Float = 0.0f,
) {
    @Id
    @GeneratedValue
    var id: Long? = null
        protected set
}
