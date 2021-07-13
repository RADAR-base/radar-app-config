//package org.radarbase.appconfig.persistence.entity
//
//import org.hibernate.annotations.Cache
//import org.hibernate.annotations.CacheConcurrencyStrategy
//import java.time.Instant
//import javax.persistence.*
//
//@Entity(name = "Protocol")
//@Table(name = "protocol")
//@Cacheable
//@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
//class ProtocolEntity {
//    @Id
//    @GeneratedValue(generator = "protocol_id_sequence")
//    var id: Long? = null
//        private set
//
//    @Column
//    lateinit var scope: String
//
//    @Column(name = "client_id")
//    lateinit var clientId: String
//
//    @Column
//    lateinit var version: String
//
//    @Column
//    @Lob
//    lateinit var contents: String
//
//    @Column(name = "last_modified_at")
//    var lastModifiedAt: Instant = Instant.now()
//}
