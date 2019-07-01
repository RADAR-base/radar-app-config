package org.radarbase.appconfig.auth

import javax.ws.rs.NameBinding

/**
 * Annotation for requests that should be authenticated.
 */
@NameBinding
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION,
        AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Authenticated
