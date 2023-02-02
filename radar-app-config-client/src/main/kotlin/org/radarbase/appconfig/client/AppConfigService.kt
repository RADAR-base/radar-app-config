package org.radarbase.appconfig.client

import java.time.Instant
import java.time.temporal.TemporalAmount
import org.radarbase.appconfig.client.api.AppConfigConfig
import org.radarbase.appconfig.client.config.AppConfigServiceConfig

class AppConfigService(
    private val client: AppConfigClient,
    private val cache: Cache<String, AppConfigResult>,
    private val config: AppConfigServiceConfig,
) {
    suspend fun config(scope: Scope): Map<String, String> {
        val cachedResult = cache.get(scope.toString())

        val validResult = if (cachedResult == null || !cachedResult.isValid()) {
            val fetchedResult = try {
                val config = client.fetchConfig(scope)
                AppConfigResult.Success(config.configMap)
            } catch (ex: NoSuchElementException) {
                AppConfigResult.NotFound()
            } catch (ex: Exception) {
                AppConfigResult.Error(ex)
            }
            cache.put(scope.toString(), fetchedResult)
            fetchedResult
        } else cachedResult

        return when (validResult) {
            is AppConfigResult.Success -> validResult.config
            is AppConfigResult.NotFound -> throw NoSuchElementException()
            is AppConfigResult.Error -> throw validResult.exception
        }
    }

    suspend fun update(config: AppConfigConfig, scope: Scope): Map<String, String> {
        val result = try {
            val newConfig = client.updateConfig(config, scope)
            AppConfigResult.Success(newConfig.configMap)
        } catch (ex: NoSuchElementException) {
            AppConfigResult.NotFound()
        } catch (ex: Exception) {
            cache.remove(scope.toString())
            throw ex
        }

        cache.put(scope.toString(), result)

        return when (result) {
            is AppConfigResult.Success -> result.config
            is AppConfigResult.NotFound -> throw NoSuchElementException()
            else -> throw IllegalStateException("No result found")
        }
    }

    sealed class AppConfigResult {
        private val time: Instant = Instant.now()

        fun isValid(validity: TemporalAmount): Boolean {
            return Instant.now() <= time + validity
        }

        internal class NotFound : AppConfigResult()
        internal class Error(val exception: Exception) : AppConfigResult()
        internal class Success(val config: Map<String, String>) : AppConfigResult()

    }

    private fun AppConfigResult.isValid() = when (this) {
        is AppConfigResult.NotFound -> isValid(this@AppConfigService.config.notFoundValidity)
        is AppConfigResult.Success -> isValid(this@AppConfigService.config.successValidity)
        is AppConfigResult.Error -> isValid(this@AppConfigService.config.errorValidity)
    }
}
