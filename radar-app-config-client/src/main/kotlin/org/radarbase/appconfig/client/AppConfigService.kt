package org.radarbase.appconfig.client

import java.time.Duration
import java.time.Instant
import java.time.temporal.TemporalAmount
import org.radarbase.appconfig.client.api.AppConfigConfig

class AppConfigService(
    private val client: AppConfigClient,
    private val cache: Cache<String, AppConfigResult>,
    private val config: AppConfigServiceConfig,
) {
    suspend fun config(scope: Scope): Map<String, String> {
        val cachedResult = cache.get(scope.toString())

        val validResult = if (cachedResult == null || !cachedResult.isValid(config)) {
            val fetchedResult = try {
                val config = client.fetchConfig(scope)
                AppConfigSuccess(config.configMap)
            } catch (ex: NoSuchElementException) {
                AppConfigNotFound()
            } catch (ex: Exception) {
                AppConfigError(ex)
            }
            cache.put(scope.toString(), fetchedResult)
            fetchedResult
        } else cachedResult

        return when (validResult) {
            is AppConfigSuccess -> validResult.config
            is AppConfigNotFound -> throw NoSuchElementException()
            is AppConfigError -> throw validResult.exception
        }
    }

    suspend fun update(config: AppConfigConfig, scope: Scope): Map<String, String> {
        val result = try {
            val newConfig = client.updateConfig(config, scope)
            AppConfigSuccess(newConfig.configMap)
        } catch (ex: NoSuchElementException) {
            AppConfigNotFound()
        } catch (ex: Exception) {
            throw ex
        }

        cache.put(scope.toString(), result)

        return when (result) {
            is AppConfigSuccess -> result.config
            is AppConfigNotFound -> throw NoSuchElementException()
            else -> throw IllegalStateException("No result found")
        }
    }
}

data class AppConfigServiceConfig(
    val notFoundValidity: Duration = Duration.ofMinutes(15),
    val errorValidity: Duration = Duration.ofSeconds(30),
    val successValidity: Duration = Duration.ofMinutes(10),
)

sealed class AppConfigResult {
    private val time: Instant = Instant.now()

    fun isValid(validity: TemporalAmount): Boolean {
        return Instant.now() <= time + validity
    }
}

class AppConfigNotFound : AppConfigResult()
class AppConfigError(val exception: Exception) : AppConfigResult()
class AppConfigSuccess(val config: Map<String, String>) : AppConfigResult()

fun AppConfigResult.isValid(config: AppConfigServiceConfig) = when (this) {
    is AppConfigNotFound -> isValid(config.notFoundValidity)
    is AppConfigSuccess -> isValid(config.successValidity)
    is AppConfigError -> isValid(config.errorValidity)
}
