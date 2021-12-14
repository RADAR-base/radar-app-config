package org.radarbase.appconfig.client

import java.io.IOException
import java.time.Duration
import java.time.Instant
import java.time.temporal.TemporalAmount
import org.radarbase.appconfig.client.api.AppConfigConfig

class AppConfigService(
    private val client: AppConfigClient,
    private val cache: Cache<String, AppConfigResult>,
    private val config: AppConfigServiceConfig,
) {
    suspend fun config(projectId: String? = null, userId: String? = null): Map<String, String> {
        val scope = when {
            userId != null -> "user.$userId"
            projectId != null -> "project.$projectId"
            else -> "global"
        }
        val result = cache.get(scope)

        when (result) {
            is AppConfigNotFound ->
                if (!result.isValid(config.notFoundValidity)) {
                    cache.remove(scope, result)
                }
                throw NoSuchElementException()
        }

        return result
            ?: try {
                val config = client.fetchConfig(projectId, userId)
                cache.putIfAbsent(scope, config)
                config
            } catch (ex: NoSuchElementException) {
                state =
                throw ex
            } catch (ex: Exception) {
                state = if (ex is when (ex) {
                    is IOException, is IllegalStateException -> State.ERROR
                    is NoSuchElementException -> State.UNAVAILABLE
                }
                throw ex
            } catch (ex: IllegalStateException) {
            } catch (ex: NoSuchElementException) {

            } catch (ex: Exception) {
                throw ex
            }
    }

    suspend fun update(config: AppConfigConfig, projectId: String? = null, userId: String? = null): Map<String, String> {
        val result = try {
            client.updateConfig(config, projectId, userId)
        } catch (ex: IOException) {

        } catch (ex: IllegalStateException) {

        } catch (ex: NoSuchElementException) {

        } catch (ex: Exception) {

            throw ex
        }

        val scope = when {
            userId != null -> "user.$userId"
            projectId != null -> "project.$projectId"
            else -> "global"
        }
        cache.put(scope, result)

        return result
    }
}

data class AppConfigServiceConfig(
    val notFoundValidity: Duration = Duration.ofMinutes(15),
    val errorValidity: Duration = Duration.ofSeconds(30),
    val successValidity: Duration = Duration.ofMinutes(10),
)

sealed interface AppConfigResult {
    val time: Instant

    fun isValid(validity: TemporalAmount): Boolean {
        return Instant.now() <= time + validity
    }
}

class AppConfigNotFound(override val time: Instant) : AppConfigResult
class AppConfigError(override val time: Instant) : AppConfigResult
class AppConfigSuccess(override val time: Instant, val config: Map<String, String>) : AppConfigResult
