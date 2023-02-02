package org.radarbase.appconfig.client

sealed interface Scope {
    object Global : Scope {
        override fun toString(): String = "\$g"
    }

    class User(val projectId: String, val userId: String) : Scope {
        override fun toString(): String = "\$u.$userId.\$p.$projectId"
    }

    class Condition(val projectId: String, val conditionId: String) : Scope {
        override fun toString(): String = "\$c.$conditionId.\$p.$projectId"
    }

    class Project(val projectId: String) : Scope {
        override fun toString(): String = "\$p.$projectId"
    }
}
