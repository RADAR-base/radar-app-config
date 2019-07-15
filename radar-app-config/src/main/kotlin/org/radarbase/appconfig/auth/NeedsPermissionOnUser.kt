package org.radarbase.appconfig.auth

import org.radarcns.auth.authorization.Permission

/**
 * Indicates that a method needs an authenticated user that has a certain permission.
 */
@Target(AnnotationTarget.FUNCTION,
        AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
annotation class NeedsPermissionOnUser(
        /**
         * Entity that the permission is needed on.
         */
        val entity: Permission.Entity,
        /**
         * Operation on given entity that the permission is needed for.
         */
        val operation: Permission.Operation,
        /** Project path parameter. */
        val projectPathParam: String,
        /** User path parameter. */
        val userPathParam: String)
