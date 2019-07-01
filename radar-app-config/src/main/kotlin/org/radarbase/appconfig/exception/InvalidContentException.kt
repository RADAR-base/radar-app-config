package org.radarbase.appconfig.exception

class InvalidContentException(s: String) : HttpApplicationException(422, "invalid_content", s)
