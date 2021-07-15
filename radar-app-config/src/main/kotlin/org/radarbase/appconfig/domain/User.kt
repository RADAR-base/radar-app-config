package org.radarbase.appconfig.domain

import org.radarbase.management.client.MPSubject

data class User(
    val id: String,
    val externalUserId: String? = null,
    val hasConfig: Boolean? = null,
)
