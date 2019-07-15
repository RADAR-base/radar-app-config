package org.radarbase.appconfig.domain

import com.fasterxml.jackson.annotation.JsonProperty
import nl.thehyve.lang.expression.*
import java.util.function.BiConsumer
import java.util.function.BinaryOperator
import java.util.function.Function
import java.util.function.Supplier
import java.util.stream.Collector
import java.util.stream.Collectors
import java.util.stream.Collectors.mapping
import java.util.stream.Collectors.toSet
import java.util.stream.Stream

data class Project(val id: Long, @JsonProperty("projectName") val name: String, @JsonProperty("humanReadableProjectName") val humanReadableName: String? = null, val location: String? = null, val organization: String? = null, val description: String? = null)

data class OAuthClient(@JsonProperty("clientId") val id: String)

data class OAuthClientList(val clients: List<OAuthClient>)

data class ConditionList(val conditions: List<Condition>)

data class Condition(val id: Long?, val name: String?, val title: String? = null, val expression: Expression, val config: Map<String, Map<String, String>>? = null)

data class GlobalConfig(val clients: Map<String, ClientConfig>) {
    companion object {
        fun fromStream(stream: Stream<ResolvedVariable>): GlobalConfig {
            return GlobalConfig(stream
                    .filter { it.id.names.isNotEmpty() }
                    .collect(Collectors.groupingBy({ it.id.names.first() }, ClientConfigCollector())))
        }
    }
}

data class ClientConfig(val clientId: String?, val config: List<SingleVariable>) {
    companion object {
        fun fromStream(clientId: String, stream: Stream<ResolvedVariable>, includeId: Boolean = false): ClientConfig {
            return ClientConfig(if (includeId) clientId else null, stream
                    .filter { it.id.names.firstOrNull() == clientId }
                    .map { (scope, id, variable) ->
                        SingleVariable(id.splitHead()!!.second.asString(), variable.asOptString(), scope.asString())
                    }
                    .collect(Collectors.toList()))
        }
    }
}

class ClientConfigCollector : Collector<ResolvedVariable, ClientConfigCollector.ClientConfigCollection, ClientConfig> {
    data class ClientConfigCollection(var clientId: String? = null, val config: MutableList<SingleVariable> = mutableListOf())

    override fun characteristics() = setOf<Collector.Characteristics>()

    override fun supplier() = Supplier { ClientConfigCollection() }

    override fun accumulator() = BiConsumer { list: ClientConfigCollection, (scope, id, variable): ResolvedVariable ->
        val (clientId, innerId) = id.splitHead() ?: return@BiConsumer
        if (list.clientId == null) {
            list.clientId = clientId
        } else if (clientId != list.clientId) {
            return@BiConsumer
        }

        list.config += SingleVariable(innerId.asString(), variable.asOptString(), scope.asString())
    }

    override fun combiner() = BinaryOperator<ClientConfigCollection> { list1, list2 ->
        if (list1.clientId == null || list2.clientId == null || list1.clientId == list2.clientId) {
            list2.clientId?.let { list1.clientId = it }
            list1.config += list2.config
        }
        list1
    }

    override fun finisher() = Function { list: ClientConfigCollection ->
        ClientConfig(list.clientId, list.config)
    }
}

data class SingleVariable(val name: String, val value: String?, val scope: String? = null)